package com.example.practice.dto.farm;

public record CameraStreamResponse(
        Long cameraId,
        String cameraName,
        String streamUrl,
        String protocol
) {
}
