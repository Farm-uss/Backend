package com.example.practice.dto.crops;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "생장 분석 상세 데이터")
public record GrowthCheckData(
        @Schema(description = "잎 개수", example = "10")
        int leafCount,
        @Schema(description = "잎 전체 픽셀 면적 합(px^2)", example = "23246.75")
        BigDecimal sizePxTotal,
        @Schema(description = "잎 개수 산정 신뢰도 임계값", example = "0.6")
        BigDecimal confidenceThreshold,
        @Schema(description = "신뢰도(0~100)", example = "53")
        int confidence,
        @Schema(description = "업로드 이미지 ID", example = "3")
        Long uploadedImageId,
        @Schema(description = "추론 ID", example = "1")
        Long inferenceId,
        @Schema(description = "모델명", example = "pe_best.pt")
        String modelName,
        @Schema(description = "모델 버전", example = "v1.0.0")
        String modelVersion,
        @Schema(description = "추론 시각(UTC)")
        OffsetDateTime inferredAt
) {
}
