package com.example.practice.service.schedule;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.crops.DiseaseCheckData;
import com.example.practice.dto.crops.GrowthCheckData;
import com.example.practice.dto.farm.CameraCaptureResponse;
import com.example.practice.entity.crops.Crops;
import com.example.practice.repository.crops.CropsRepository;
import com.example.practice.service.crops.VisionInferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class CameraCaptureInferenceService {

    private static final long IMAGE_DOWNLOAD_TIMEOUT_MS = 10000;

    private final CropsRepository cropsRepository;
    private final VisionInferenceService visionInferenceService;
    private final WebClient.Builder webClientBuilder;

    public ScheduledCaptureInferenceResult runAfterCapture(Long farmId, CameraCaptureResponse capture) {
        Crops crop = resolveCurrentCrop(farmId);
        CapturedImage image = downloadCapturedImage(capture.imageUrl(), capture.contentType());

        InferenceStepResult disease = runDiseaseInference(farmId, crop, image, capture);
        InferenceStepResult growth = runGrowthInference(farmId, crop, image, capture);

        return new ScheduledCaptureInferenceResult(
                crop.getCropsId(),
                disease.inferenceId(),
                disease.label(),
                disease.errorMessage(),
                growth.inferenceId(),
                growth.leafCount(),
                growth.errorMessage()
        );
    }

    private InferenceStepResult runDiseaseInference(
            Long farmId,
            Crops crop,
            CapturedImage image,
            CameraCaptureResponse capture
    ) {
        try {
            DiseaseCheckData disease = visionInferenceService.inferScheduledDiseaseAndSave(
                    farmId,
                    crop.getCropsId(),
                    image.bytes(),
                    image.filename(),
                    image.contentType(),
                    capture.cameraId(),
                    capture.captureId(),
                    capture.capturedAt()
            );
            return InferenceStepResult.success(disease.inferenceId(), disease.diseaseName(), null);
        } catch (Exception e) {
            return InferenceStepResult.failure(toFailureMessage(e));
        }
    }

    private InferenceStepResult runGrowthInference(
            Long farmId,
            Crops crop,
            CapturedImage image,
            CameraCaptureResponse capture
    ) {
        try {
            GrowthCheckData growth = visionInferenceService.inferScheduledGrowthAndSave(
                    farmId,
                    crop.getCropsId(),
                    image.bytes(),
                    image.filename(),
                    image.contentType(),
                    capture.cameraId(),
                    capture.captureId(),
                    capture.capturedAt()
            );
            return InferenceStepResult.success(growth.inferenceId(), null, growth.leafCount());
        } catch (Exception e) {
            return InferenceStepResult.failure(toFailureMessage(e));
        }
    }

    private Crops resolveCurrentCrop(Long farmId) {
        List<Crops> candidates = cropsRepository.findCurrentCropCandidatesByFarmId(farmId);
        if (candidates.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "current crop not found for farm");
        }
        return candidates.get(0);
    }

    private CapturedImage downloadCapturedImage(String imageUrl, String contentType) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "captured image url is empty");
        }

        byte[] bytes = imageUrl.startsWith("/")
                ? readLocalImage(imageUrl)
                : readRemoteImage(imageUrl);

        String filename = filenameFromUrl(imageUrl);
        String resolvedContentType = contentType == null || contentType.isBlank()
                ? MediaType.IMAGE_JPEG_VALUE
                : contentType;
        return new CapturedImage(bytes, filename, resolvedContentType);
    }

    private byte[] readLocalImage(String imagePath) {
        try {
            return Files.readAllBytes(Path.of(imagePath));
        } catch (IOException e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "failed to read captured image");
        }
    }

    private byte[] readRemoteImage(String imageUrl) {
        try {
            ByteArrayResource resource = webClientBuilder.build()
                    .get()
                    .uri(imageUrl)
                    .retrieve()
                    .bodyToMono(ByteArrayResource.class)
                    .timeout(Duration.ofMillis(IMAGE_DOWNLOAD_TIMEOUT_MS))
                    .onErrorMap(TimeoutException.class,
                            ex -> new AppException(HttpStatus.GATEWAY_TIMEOUT, "captured image download timeout"))
                    .onErrorMap(WebClientRequestException.class,
                            ex -> new AppException(HttpStatus.SERVICE_UNAVAILABLE, "captured image is unavailable"))
                    .onErrorMap(WebClientResponseException.class,
                            ex -> new AppException(HttpStatus.BAD_GATEWAY,
                                    "captured image download error: " + ex.getStatusCode().value()))
                    .block();

            if (resource == null || resource.contentLength() == 0) {
                throw new AppException(HttpStatus.BAD_GATEWAY, "empty captured image");
            }
            return resource.getByteArray();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "failed to download captured image");
        }
    }

    private String filenameFromUrl(String imageUrl) {
        String normalized = imageUrl;
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int slashIndex = normalized.lastIndexOf('/');
        String filename = slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
        return filename.isBlank() ? "scheduled-capture.jpg" : filename;
    }

    private String toFailureMessage(Exception e) {
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return e.getClass().getSimpleName();
        }
        return e.getMessage();
    }

    private record CapturedImage(byte[] bytes, String filename, String contentType) {
    }

    private record InferenceStepResult(Long inferenceId, String label, Integer leafCount, String errorMessage) {
        private static InferenceStepResult success(Long inferenceId, String label, Integer leafCount) {
            return new InferenceStepResult(inferenceId, label, leafCount, null);
        }

        private static InferenceStepResult failure(String errorMessage) {
            return new InferenceStepResult(null, null, null, errorMessage);
        }
    }

    public record ScheduledCaptureInferenceResult(
            Long cropsId,
            Long diseaseInferenceId,
            String diseaseName,
            String diseaseErrorMessage,
            Long growthInferenceId,
            Integer leafCount,
            String growthErrorMessage
    ) {
        public boolean hasFailures() {
            return diseaseErrorMessage != null || growthErrorMessage != null;
        }

        public String toHistoryMessage(Long captureId) {
            return "camera capture and inference " + (hasFailures() ? "partial failure" : "success")
                    + ": captureId=" + captureId
                    + ", cropsId=" + cropsId
                    + ", diseaseInferenceId=" + diseaseInferenceId
                    + ", diseaseName=" + diseaseName
                    + ", diseaseError=" + diseaseErrorMessage
                    + ", growthInferenceId=" + growthInferenceId
                    + ", leafCount=" + leafCount
                    + ", growthError=" + growthErrorMessage;
        }
    }
}
