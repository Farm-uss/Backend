package com.example.practice.dto.farm;

import java.time.OffsetDateTime;

public record CameraStreamResponse(
        String streamUrl,
        String protocol,
        OffsetDateTime expiresAt
) {
}
