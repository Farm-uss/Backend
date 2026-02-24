package com.example.practice.controller.crops;

import com.example.practice.common.config.TokenAuthFilter;
import com.example.practice.dto.crops.GddSummaryResponse;
import com.example.practice.dto.crops.GddTimeSeriesResponse;
import com.example.practice.dto.crops.GrowthDiaryCardResponse;
import com.example.practice.dto.crops.GrowthDiaryDetailResponse;
import com.example.practice.dto.crops.GrowthMetricResponse;
import com.example.practice.entity.crops.GrowthMetricType;
import com.example.practice.service.crops.GddSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Farm Crop GDD", description = "농장 작물 GDD 요약 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/farms")
public class FarmCropGddController {

    private final GddSummaryService gddSummaryService;

    @Operation(summary = "GDD 요약", description = "목표 성장일/현재 성장일/예상 수확일 요약을 반환합니다.")
    @GetMapping("/{farmId}/crops/{cropsId}/gdd/summary")
    public GddSummaryResponse getSummary(
            @PathVariable Long farmId,
            @PathVariable Long cropsId,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal user
    ) {
        return gddSummaryService.getSummary(farmId, cropsId, user.id());
    }

    @Operation(summary = "GDD 시계열", description = "기간(from~to)의 일별 GDD 및 누적 GDD를 반환합니다.")
    @GetMapping("/{farmId}/crops/{cropsId}/gdd")
    public List<GddTimeSeriesResponse> getTimeSeries(
            @PathVariable Long farmId,
            @PathVariable Long cropsId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal user
    ) {
        return gddSummaryService.getTimeSeries(farmId, cropsId, user.id(), from, to);
    }

    @Operation(summary = "성장 지표 시계열", description = "기간(from~to)의 특정 성장 지표(metric) 시계열을 반환합니다.")
    @GetMapping("/{farmId}/crops/{cropsId}/growth-metrics")
    public List<GrowthMetricResponse> getGrowthMetrics(
            @PathVariable Long farmId,
            @PathVariable Long cropsId,
            @RequestParam GrowthMetricType metric,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal user
    ) {
        return gddSummaryService.getGrowthMetrics(farmId, cropsId, user.id(), metric, from, to);
    }

    @Operation(summary = "성장일기 월별 목록", description = "특정 월의 날짜별 카드 리스트를 반환합니다.")
    @GetMapping("/{farmId}/crops/{cropsId}/growth-diary")
    public List<GrowthDiaryCardResponse> getGrowthDiary(
            @PathVariable Long farmId,
            @PathVariable Long cropsId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal user
    ) {
        return gddSummaryService.getGrowthDiary(farmId, cropsId, user.id(), year, month);
    }

    @Operation(summary = "성장일기 날짜 상세", description = "특정 날짜의 성장일기 상세를 반환합니다.")
    @GetMapping("/{farmId}/crops/{cropsId}/growth-diary/{date}")
    public GrowthDiaryDetailResponse getGrowthDiaryDetail(
            @PathVariable Long farmId,
            @PathVariable Long cropsId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal user
    ) {
        return gddSummaryService.getGrowthDiaryDetail(farmId, cropsId, user.id(), date);
    }
}
