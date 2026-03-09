package com.example.practice.dto.crops;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "생장 분석 응답")
public record GrowthInferenceCheckResponse(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,
        @Schema(description = "응답 코드", example = "200")
        String code,
        @Schema(description = "생장 분석 데이터")
        GrowthCheckData data
) {
    public static GrowthInferenceCheckResponse ok(GrowthCheckData data) {
        return new GrowthInferenceCheckResponse(true, "200", data);
    }
}
