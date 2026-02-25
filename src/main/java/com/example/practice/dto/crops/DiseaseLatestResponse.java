package com.example.practice.dto.crops;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "작물 최신 병해 추론 결과")
public record DiseaseLatestResponse(
        @Schema(description = "정상/비정상 상태", example = "ABNORMAL")
        String status,
        @Schema(description = "질병명", example = "leaf_blight")
        String diseaseName,
        @Schema(description = "신뢰도", example = "0.62")
        BigDecimal confidence,
        @Schema(description = "측정 시각(UTC)")
        OffsetDateTime measuredAt
) {
}
