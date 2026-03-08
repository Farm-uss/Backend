package com.example.practice.dto.crops;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "병해충 분석 응답")
public record VisionInferenceCheckResponse(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,
        @Schema(description = "응답 코드", example = "200")
        String code,
        @Schema(description = "병해충 분석 데이터")
        DiseaseCheckData data
) {
    public static VisionInferenceCheckResponse ok(DiseaseCheckData data) {
        return new VisionInferenceCheckResponse(true, "200", data);
    }
}
