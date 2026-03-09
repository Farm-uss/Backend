package com.example.practice.dto.crops;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GDD 요약 응답")
public record GddSummaryCheckResponse(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,
        @Schema(description = "응답 코드", example = "200")
        String code,
        @Schema(description = "GDD 요약 데이터")
        GddSummaryResponse data
) {
    public static GddSummaryCheckResponse ok(GddSummaryResponse data) {
        return new GddSummaryCheckResponse(true, "200", data);
    }
}
