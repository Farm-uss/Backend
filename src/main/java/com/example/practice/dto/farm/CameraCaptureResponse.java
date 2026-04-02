package com.example.practice.dto.farm;

import java.time.OffsetDateTime;

public record CameraCaptureResponse(
        Long captureId,
        Long cameraId,
        String cameraName,
        String imageUrl,
        OffsetDateTime capturedAt,
        String contentType
) {
}
