package com.example.practice.controller.device;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.Device.IrrigationRequest;
import com.example.practice.dto.Device.IrrigationResponse;
import com.example.practice.service.device.IrrigationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/irrigation")
@RequiredArgsConstructor
public class IrrigationController {

    private final IrrigationService irrigationService;

    /**
     * 수동 관수 명령 등록
     * POST /api/irrigation
     * Body:
     * { "deviceId": 1, "command": "PUMP_ON", "durationSeconds": 10 }
     * { "deviceId": 1, "command": "PUMP_OFF" }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IrrigationResponse> createCommand(
            @Valid @RequestBody IrrigationRequest request) {
        return ApiResponse.success(irrigationService.createCommand(request));
    }

    /**
     * 라즈베리파이 → PENDING 관수 명령 조회
     * GET /api/irrigation/pending?deviceId=1
     */
    @GetMapping("/pending")
    public ApiResponse<List<IrrigationResponse>> getPendingCommands(
            @RequestParam Long deviceId) {
        return ApiResponse.success(irrigationService.getPendingCommands(deviceId));
    }

    /**
     * 라즈베리파이 → 명령 실행 완료 보고
     * PATCH /api/irrigation/{commandId}/ack?deviceId=1&success=true
     */
    @PatchMapping("/{commandId}/ack")
    public ApiResponse<IrrigationResponse> acknowledge(
            @PathVariable Long commandId,
            @RequestParam Long deviceId,
            @RequestParam(defaultValue = "true") boolean success) {
        return ApiResponse.success(
                irrigationService.acknowledge(commandId, deviceId, success)
        );
    }
}