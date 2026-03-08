package com.example.practice.dto.crops;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "병해충 분석 상세 데이터")
public record DiseaseCheckData(
        @Schema(description = "질병 감지 여부(0: 정상, 1: 질병)", example = "1")
        int diseaseStatus,
        @Schema(description = "질병 코드", example = "a7")
        String diseaseId,
        @Schema(description = "질병명", example = "잎마름병")
        String diseaseName,
        @Schema(description = "질병 설명", example = "잎 가장자리부터 갈변하며 점차 확산됩니다.")
        String diseaseDescription,
        @Schema(description = "신뢰도(0~100)", example = "99")
        int confidence,
        @Schema(description = "발생 원인 목록")
        List<String> causes,
        @Schema(description = "대표 증상 목록")
        List<String> symptoms,
        @Schema(description = "해결/관리 방법 목록")
        List<String> solutions,
        @Schema(description = "업로드 이미지 ID", example = "3")
        Long uploadedImageId,
        @Schema(description = "추론 ID", example = "1")
        Long inferenceId,
        @Schema(description = "모델명", example = "farmus-placeholder-model")
        String modelName,
        @Schema(description = "모델 버전", example = "v0.1.0")
        String modelVersion,
        @Schema(description = "추론 시각(UTC)")
        OffsetDateTime inferredAt
) {
}
