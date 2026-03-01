package com.example.practice.controller.device;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.Device.SensorReadingResponse;
import com.example.practice.dto.Device.SensorReadingSaveRequest;
import com.example.practice.service.device.SensorReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;


@Tag(name = "sensor-readings", description = "센서 읽기 API")
@RestController
@RequestMapping("/api/sensor-readings")
@RequiredArgsConstructor
public class SensorReadingController {

    private final SensorReadingService sensorReadingService;

    /** 센서 측정값 개별 저장 */
    @Operation(summary = "센서 측정값 개별 저장")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SensorReadingResponse> save(@Valid @RequestBody SensorReadingSaveRequest request) {
        return ApiResponse.success(sensorReadingService.save(request));
    }

    /** 센서별 측정값 페이징 조회 */
    @Operation(summary = "센서별 측정값 페이징 조회")
    @GetMapping
    public ApiResponse<Page<SensorReadingResponse>> getPage(
            @RequestParam Long sensorId,
            @PageableDefault(size = 20, sort = "measuredAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ApiResponse.success(sensorReadingService.getPageBySensorId(sensorId, pageable));
    }

    /** 센서 기준 기간 조회 */
    @Operation(summary = "센서 기준 기간 조회")
    @GetMapping("/range")
    public ApiResponse<List<SensorReadingResponse>> getByPeriod(
            @RequestParam Long sensorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ApiResponse.success(sensorReadingService.getByPeriod(sensorId, from, to));
    }

    /** 장치 기준 기간 조회 (모든 센서 타입 포함) */
    @Operation(summary = "장치 기준 기간 조회 (모든 센서 타입 포함)")
    @GetMapping("/device-range")
    public ApiResponse<List<SensorReadingResponse>> getByDeviceAndPeriod(
            @RequestParam Long deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ApiResponse.success(sensorReadingService.getByDeviceIdAndPeriod(deviceId, from, to));
    }
}
