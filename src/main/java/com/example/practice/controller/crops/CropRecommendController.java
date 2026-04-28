package com.example.practice.controller.crops;

import com.example.practice.dto.crops.CropRecommendResponse;
import com.example.practice.service.crops.CropRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CropRecommend", description = "작물 추천 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/api/v1/crops-recommend")
@RequiredArgsConstructor
public class CropRecommendController {

    private final CropRecommendService cropRecommendService;

    /**
     * GET /api/v1/crops/recommend?deviceId=1
     *
     * 해당 디바이스의 모든 센서 최신 측정값을 수집하여
     * crop_environment_standard 전체와 비교 후 작물을 추천한다.
     * 6개 센서 항목 중 3개 이상 범위 내에 드는 작물만 반환한다.
     */
    @GetMapping
    @Operation(summary = "작물 추천")
    public ResponseEntity<CropRecommendResponse> recommend(@RequestParam Long deviceId) {
        return ResponseEntity.ok(cropRecommendService.recommend(deviceId));
    }
}
