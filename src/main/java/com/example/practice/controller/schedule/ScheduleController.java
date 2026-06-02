package com.example.practice.controller.schedule;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.schedule.ConditionScheduleResponse;
import com.example.practice.dto.schedule.CreateConditionScheduleRequest;
import com.example.practice.dto.schedule.CreateTimeScheduleRequest;
import com.example.practice.dto.schedule.ScheduleEnabledUpdateRequest;
import com.example.practice.dto.schedule.ScheduleExecutionHistoryResponse;
import com.example.practice.dto.schedule.ScheduleListItemResponse;
import com.example.practice.dto.schedule.ScheduleResponse;
import com.example.practice.dto.schedule.TimeScheduleResponse;
import com.example.practice.dto.schedule.UpdateConditionScheduleRequest;
import com.example.practice.dto.schedule.UpdateTimeScheduleRequest;
import com.example.practice.service.schedule.ScheduleCommandService;
import com.example.practice.service.schedule.ScheduleQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "schedules", description = "자동화 스케줄 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleCommandService scheduleCommandService;
    private final ScheduleQueryService scheduleQueryService;

    @Operation(summary = "시간 기반 스케줄 등록")
    @PostMapping("/time-based")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TimeScheduleResponse> createTimeSchedule(@RequestBody CreateTimeScheduleRequest request) {
        return ApiResponse.success(scheduleCommandService.createTimeSchedule(request));
    }

    @Operation(summary = "조건 기반 스케줄 등록")
    @PostMapping("/condition-based")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ConditionScheduleResponse> createConditionSchedule(@RequestBody CreateConditionScheduleRequest request) {
        return ApiResponse.success(scheduleCommandService.createConditionSchedule(request));
    }

    @Operation(summary = "시간 기반 스케줄 수정")
    @PutMapping("/{scheduleId}/time-based")
    public ApiResponse<TimeScheduleResponse> updateTimeSchedule(@PathVariable Long scheduleId,
                                                                @RequestBody UpdateTimeScheduleRequest request) {
        return ApiResponse.success(scheduleCommandService.updateTimeSchedule(scheduleId, request));
    }

    @Operation(summary = "조건 기반 스케줄 수정")
    @PutMapping("/{scheduleId}/condition-based")
    public ApiResponse<ConditionScheduleResponse> updateConditionSchedule(@PathVariable Long scheduleId,
                                                                          @RequestBody UpdateConditionScheduleRequest request) {
        return ApiResponse.success(scheduleCommandService.updateConditionSchedule(scheduleId, request));
    }

    @Operation(summary = "농장별 스케줄 목록 조회")
    @GetMapping
    public ApiResponse<List<ScheduleListItemResponse>> getByFarmId(@RequestParam Long farmId) {
        return ApiResponse.success(scheduleQueryService.getByFarmId(farmId));
    }

    @Operation(summary = "시간 기반 스케줄 상세 조회")
    @GetMapping("/{scheduleId}/time-based")
    public ApiResponse<TimeScheduleResponse> getTimeScheduleById(@PathVariable Long scheduleId) {
        return ApiResponse.success(scheduleQueryService.getTimeScheduleById(scheduleId));
    }

    @Operation(summary = "조건 기반 스케줄 상세 조회")
    @GetMapping("/{scheduleId}/condition-based")
    public ApiResponse<ConditionScheduleResponse> getConditionScheduleById(@PathVariable Long scheduleId) {
        return ApiResponse.success(scheduleQueryService.getConditionScheduleById(scheduleId));
    }

    @Operation(summary = "스케줄 삭제")
    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long scheduleId) {
        scheduleCommandService.delete(scheduleId);
        return ApiResponse.success();
    }

    @Operation(summary = "스케줄 활성화 여부 변경")
    @PatchMapping("/{scheduleId}/enabled")
    public ApiResponse<ScheduleResponse> updateEnabled(@PathVariable Long scheduleId,
                                                       @RequestBody ScheduleEnabledUpdateRequest request) {
        return ApiResponse.success(scheduleCommandService.updateEnabled(scheduleId, request));
    }

    @Operation(summary = "농장 전체 스케줄 실행 이력 조회")
    @GetMapping("/histories")
    public ApiResponse<List<ScheduleExecutionHistoryResponse>> getHistoriesByFarmId(@RequestParam Long farmId) {
        return ApiResponse.success(scheduleQueryService.getHistoriesByFarmId(farmId));
    }

    @Operation(summary = "특정 스케줄 실행 이력 조회")
    @GetMapping("/{scheduleId}/histories")
    public ApiResponse<List<ScheduleExecutionHistoryResponse>> getHistoriesByScheduleId(@PathVariable Long scheduleId) {
        return ApiResponse.success(scheduleQueryService.getHistoriesByScheduleId(scheduleId));
    }
}
