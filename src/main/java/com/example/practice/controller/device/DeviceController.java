package com.example.practice.controller.device;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.Device.DeviceRegisterRequest;
import com.example.practice.dto.Device.DeviceResponse;
import com.example.practice.dto.Device.DeviceStatusUpdateRequest;
import com.example.practice.service.device.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Tag(name = "devices", description = "라즈베리파이 API")
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /** 라즈베리파이 최초 등록 */
    @Operation(summary = "라즈베리파이 최초 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DeviceResponse> register(@Valid @RequestBody DeviceRegisterRequest request) {
        return ApiResponse.success(deviceService.register(request));
    }

    /** 단일 장치 조회 */
    @Operation(summary = "단일 장치 조회")
    @GetMapping("/{deviceId}")
    public ApiResponse<DeviceResponse> getById(@PathVariable Long deviceId) {
        return ApiResponse.success(deviceService.getById(deviceId));
    }

    /** 농장 기준 전체 장치 목록 */
    @Operation(summary = "농장 기준 전체 장치 목록")
    @GetMapping
    public ApiResponse<List<DeviceResponse>> getByFarmId(@RequestParam Long farmId) {
        return ApiResponse.success(deviceService.getByFarmId(farmId));
    }

    /** 장치 상태 변경 (ACTIVE / INACTIVE) */
    @Operation(summary =  "장치 상태 변경 (ACTIVE / INACTIVE)")
    @PatchMapping("/{deviceId}/status")
    public ApiResponse<DeviceResponse> updateStatus(
            @PathVariable Long deviceId,
            @Valid @RequestBody DeviceStatusUpdateRequest request) {
        return ApiResponse.success(deviceService.updateStatus(deviceId, request));
    }

    /** 장치 삭제 */
    @Operation(summary = "장치 삭제")
    @DeleteMapping("/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long deviceId) {
        deviceService.delete(deviceId);
        return ApiResponse.success();
    }
}
