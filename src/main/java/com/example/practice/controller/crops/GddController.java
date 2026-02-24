package com.example.practice.controller.crops;

import com.example.practice.service.crops.GddIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "GDD Ingestion", description = "GDD 수집/적재 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/api/v1/crops")
public class GddController {

    private final GddIngestionService service;

    public GddController(GddIngestionService service) {
        this.service = service;
    }

    @Operation(
            summary = "GDD 수동 적재",
            description = "특정 작물의 최근 N일 GDD를 외부 OpenAPI에서 조회해 crop_gdd_daily 테이블에 저장합니다."
    )
    @PostMapping("/{cropId}/gdd/refresh")
    public Map<String, Object> refresh(
            @Parameter(description = "작물 ID", example = "10")
            @PathVariable Long cropId,
            @Parameter(description = "조회/적재 기간(일)", example = "21")
            @RequestParam(defaultValue = "21") int days
    ) {
        int saved = service.refresh(cropId, days);
        return Map.of("cropId", cropId, "days", days, "saved", saved);
    }
}
