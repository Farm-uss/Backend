package com.example.practice.dto.farm;

import java.time.OffsetDateTime;

public record CameraStreamResponse(
        Long cameraId,
        String cameraName,
        String streamUrl,
        String protocol,
        OffsetDateTime expiresAt
) {
}
