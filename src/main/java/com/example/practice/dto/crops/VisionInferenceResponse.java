package com.example.practice.dto.crops;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Vision 추론 저장 결과")
public record VisionInferenceResponse(
        @Schema(description = "캡처 ID", example = "3")
        Long captureId,
        @Schema(description = "추론 ID", example = "1")
        Long inferenceId,
        @Schema(description = "질병 판정명", example = "unknown")
        String disease,
        @Schema(description = "신뢰도", example = "0.62")
        BigDecimal confidence,
        @Schema(description = "비정상 여부", example = "true")
        boolean abnormal,
        @Schema(description = "비정상 사유", example = "low_confidence")
        String abnormalReason,
        @Schema(description = "모델명", example = "farmus-placeholder-model")
        String modelName,
        @Schema(description = "모델 버전", example = "v0.1.0")
        String modelVersion,
        @Schema(description = "추론 시각(UTC)")
        OffsetDateTime inferredAt
) {
}
