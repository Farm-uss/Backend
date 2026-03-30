package com.example.practice.service.farm;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.farm.CameraCaptureResponse;
import com.example.practice.dto.farm.CameraStreamResponse;
import com.example.practice.entity.crops.ImageCapture;
import com.example.practice.entity.device.Camera;
import com.example.practice.repository.crops.ImageCaptureRepository;
import com.example.practice.repository.device.CameraRepository;
import com.example.practice.repository.farm.FarmMemberRepository;
import com.example.practice.service.aws.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class FarmCameraService {

    private final FarmMemberRepository farmMemberRepository;
    private final CameraRepository cameraRepository;
    private final ImageCaptureRepository imageCaptureRepository;
    private final AwsS3Service awsS3Service;
    private final WebClient.Builder webClientBuilder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${camera.stream.playback-base-url:}")
    private String playbackBaseUrl;

    @Value("${camera.stream.ttl-minutes:60}")
    private long ttlMinutes;

    @Value("${camera.capture.timeout-ms:10000}")
    private long captureTimeoutMs;

    @Transactional(readOnly = true)
    public CameraStreamResponse getFarmCameraStream(Long farmId, Long userId) {
        if (!farmMemberRepository.existsByFarmIdAndUserId(farmId, userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "farm access denied");
        }

        Camera camera = cameraRepository.findFirstByDevice_FarmIdAndPrimaryTrueOrderByCameraIdAsc(farmId)
                .or(() -> cameraRepository.findFirstByDevice_FarmIdOrderByPrimaryDescCameraIdAsc(farmId))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "camera not configured for farm"));

        if (playbackBaseUrl == null || playbackBaseUrl.isBlank()) {
            throw new AppException(HttpStatus.NOT_FOUND, "camera stream not configured for farm");
        }

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(Math.max(ttlMinutes, 1));
        String streamUrl = buildStreamUrl(camera.getStreamKey(), camera.getStreamProtocol());
        return new CameraStreamResponse(
                camera.getCameraId(),
                camera.getName(),
                streamUrl,
                camera.getStreamProtocol(),
                expiresAt
        );
    }

    @Transactional
    public CameraCaptureResponse captureFarmCamera(Long farmId, Long cameraId, Long userId) {
        if (!farmMemberRepository.existsByFarmIdAndUserId(farmId, userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "farm access denied");
        }

        return captureCameraInternal(farmId, cameraId);
    }

    @Transactional
    public CameraCaptureResponse captureFarmCameraForSchedule(Long farmId, Long cameraId) {
        return captureCameraInternal(farmId, cameraId);
    }

    private CameraCaptureResponse captureCameraInternal(Long farmId, Long cameraId) {
        Camera camera = resolveCamera(farmId, cameraId);
        if (camera.getCaptureEndpoint() == null || camera.getCaptureEndpoint().isBlank()) {
            throw new AppException(HttpStatus.NOT_FOUND, "camera capture endpoint not configured");
        }

        byte[] capturedBytes = requestCapture(camera.getCaptureEndpoint());
        OffsetDateTime capturedAt = OffsetDateTime.now(ZoneOffset.UTC);
        String imageUrl = uploadCapturedImage(capturedBytes, camera.getCameraId(), capturedAt);

        ImageCapture capture = new ImageCapture();
        capture.setCameraId(camera.getCameraId());
        capture.setCapturedAt(capturedAt);
        capture.setImagePath(imageUrl);
        capture = imageCaptureRepository.save(capture);

        return new CameraCaptureResponse(
                capture.getCaptureId(),
                camera.getCameraId(),
                camera.getName(),
                imageUrl,
                capturedAt,
                MediaType.IMAGE_JPEG_VALUE
        );
    }

    private Camera resolveCamera(Long farmId, Long cameraId) {
        if (cameraId != null) {
            return cameraRepository.findByCameraIdAndDevice_FarmId(cameraId, farmId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "camera not found in farm"));
        }

        return cameraRepository.findFirstByDevice_FarmIdAndPrimaryTrueOrderByCameraIdAsc(farmId)
                .or(() -> cameraRepository.findFirstByDevice_FarmIdOrderByPrimaryDescCameraIdAsc(farmId))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "camera not configured for farm"));
    }

    private String buildStreamUrl(String streamKey, String protocol) {
        String normalizedBaseUrl = playbackBaseUrl.endsWith("/")
                ? playbackBaseUrl.substring(0, playbackBaseUrl.length() - 1)
                : playbackBaseUrl;

        if ("HLS".equalsIgnoreCase(protocol)) {
            return normalizedBaseUrl + "/" + streamKey + "/index.m3u8";
        }

        return normalizedBaseUrl + "/" + streamKey;
    }

    private byte[] requestCapture(String captureEndpoint) {
        try {
            ByteArrayResource resource = webClientBuilder.build()
                    .post()
                    .uri(captureEndpoint)
                    .retrieve()
                    .bodyToMono(ByteArrayResource.class)
                    .timeout(Duration.ofMillis(Math.max(captureTimeoutMs, 1000)))
                    .onErrorMap(TimeoutException.class,
                            ex -> new AppException(HttpStatus.GATEWAY_TIMEOUT, "camera capture timeout"))
                    .onErrorMap(WebClientRequestException.class,
                            ex -> new AppException(HttpStatus.SERVICE_UNAVAILABLE, "camera capture is unavailable"))
                    .onErrorMap(WebClientResponseException.class,
                            ex -> new AppException(HttpStatus.BAD_GATEWAY,
                                    "camera capture error: " + ex.getStatusCode().value()))
                    .block();

            if (resource == null || resource.contentLength() == 0) {
                throw new AppException(HttpStatus.BAD_GATEWAY, "empty capture response");
            }
            return resource.getByteArray();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "failed to request camera capture");
        }
    }

    private String uploadCapturedImage(byte[] imageBytes, Long cameraId, OffsetDateTime capturedAt) {
        String originalFilename = "camera-" + cameraId + "-" + capturedAt.toEpochSecond() + ".jpg";
        try {
            String uploadedUrl = awsS3Service.upload(imageBytes, originalFilename, MediaType.IMAGE_JPEG_VALUE, "captures");
            if (uploadedUrl != null && !uploadedUrl.isBlank()) {
                return uploadedUrl;
            }
        } catch (RuntimeException ignored) {
            // S3 설정이 없는 환경에서는 로컬 저장 경로로 fallback.
        }
        return saveCaptureImage(imageBytes, originalFilename);
    }

    private String saveCaptureImage(byte[] imageBytes, String originalFilename) {
        String captureDirPath = uploadDir.endsWith("/") ? uploadDir + "captures/" : uploadDir + "/captures/";
        File captureDir = new File(captureDirPath);
        if (!captureDir.exists() && !captureDir.mkdirs()) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to create capture directory");
        }

        String extension = extractExtension(originalFilename);
        String savedName = UUID.randomUUID() + extension;
        File target = new File(captureDir, savedName);

        try {
            Files.write(target.toPath(), imageBytes);
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to save capture image");
        }
        return target.getAbsolutePath();
    }

    private String extractExtension(String name) {
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return ".jpg";
        }
        return name.substring(idx);
    }
}
