package com.example.practice.controller.device;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.Device.SensorRegisterRequest;
import com.example.practice.dto.Device.SensorResponse;
import com.example.practice.service.device.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "sensor", description = "센서 API")
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    /** 센서 등록 */
    @Operation(summary = "센서 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SensorResponse> register(@Valid @RequestBody SensorRegisterRequest request) {
        return ApiResponse.success(sensorService.register(request));
    }

    /** 장치 기준 센서 목록 */
    @Operation(summary = "장치 기준 센서 목록")
    @GetMapping
    public ApiResponse<List<SensorResponse>> getByDeviceId(@RequestParam Long deviceId) {
        return ApiResponse.success(sensorService.getByDeviceId(deviceId));
    }

    /** 단일 센서 조회 */
    @Operation(summary = "단일 센서 조회")
    @GetMapping("/{sensorId}")
    public ApiResponse<SensorResponse> getById(@PathVariable Long sensorId) {
        return ApiResponse.success(sensorService.getById(sensorId));
    }

    /** 센서 삭제 */
    @Operation(summary = "센서 삭제")
    @DeleteMapping("/{sensorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long sensorId) {
        sensorService.delete(sensorId);
        return ApiResponse.success();
    }
}

