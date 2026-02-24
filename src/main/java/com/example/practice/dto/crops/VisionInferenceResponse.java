package com.example.practice.dto.crops;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VisionInferenceResponse(
        Long captureId,
        Long inferenceId,
        String disease,
        BigDecimal confidence,
        boolean abnormal,
        String abnormalReason,
        String modelName,
        String modelVersion,
        OffsetDateTime inferredAt
) {
}
