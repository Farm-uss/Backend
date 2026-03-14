package com.example.practice.service.farm;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.farm.CameraStreamResponse;
import com.example.practice.entity.device.Camera;
import com.example.practice.repository.device.CameraRepository;
import com.example.practice.repository.farm.FarmMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class FarmCameraService {

    private final FarmMemberRepository farmMemberRepository;
    private final CameraRepository cameraRepository;

    @Value("${camera.stream.playback-base-url:}")
    private String playbackBaseUrl;

    @Value("${camera.stream.ttl-minutes:60}")
    private long ttlMinutes;

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

    private String buildStreamUrl(String streamKey, String protocol) {
        String normalizedBaseUrl = playbackBaseUrl.endsWith("/")
                ? playbackBaseUrl.substring(0, playbackBaseUrl.length() - 1)
                : playbackBaseUrl;

        if ("HLS".equalsIgnoreCase(protocol)) {
            return normalizedBaseUrl + "/" + streamKey + "/index.m3u8";
        }

        return normalizedBaseUrl + "/" + streamKey;
    }
}
