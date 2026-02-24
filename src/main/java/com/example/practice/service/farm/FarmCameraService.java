package com.example.practice.service.farm;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.farm.CameraStreamResponse;
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

    @Value("${camera.stream.default-url:}")
    private String defaultStreamUrl;

    @Value("${camera.stream.protocol:MJPEG}")
    private String protocol;

    @Value("${camera.stream.ttl-minutes:60}")
    private long ttlMinutes;

    @Transactional(readOnly = true)
    public CameraStreamResponse getFarmCameraStream(Long farmId, Long userId) {
        if (!farmMemberRepository.existsByFarmIdAndUserId(farmId, userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "farm access denied");
        }

        if (defaultStreamUrl == null || defaultStreamUrl.isBlank()) {
            throw new AppException(HttpStatus.NOT_FOUND, "camera stream not configured for farm");
        }

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(Math.max(ttlMinutes, 1));
        return new CameraStreamResponse(defaultStreamUrl, protocol, expiresAt);
    }
}
