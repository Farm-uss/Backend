package com.example.practice.controller.device;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.Device.CommandRequest;
import com.example.practice.dto.Device.CommandResponse;
import com.example.practice.service.device.DeviceCommandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "led", description = "LED API")
@RestController
@RequestMapping("/api/led")
@RequiredArgsConstructor
public class DeviceCommandController {

    private final DeviceCommandService commandService;

    /**
     * 프론트 → LED 켜기/끄기
     * POST /api/led
     * Body: { "commandType": "LED_ON" }
     *       { "commandType": "LED_OFF" }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommandResponse> createCommand(
            @Valid @RequestBody CommandRequest request) {
        return ApiResponse.success(commandService.createCommand(request));
    }

    /**
     * 라즈베리파이 → PENDING 명령 조회 (3초마다 폴링)
     * GET /api/led/pending?deviceId=1
     */
    @GetMapping("/pending")
    public ApiResponse<List<CommandResponse>> getPendingCommands(
            @RequestParam Long deviceId) {
        return ApiResponse.success(commandService.getPendingCommands(deviceId));
    }

    /**
     * 라즈베리파이 → 명령 실행 완료 보고
     * PATCH /api/led/{commandId}/ack?deviceId=1&success=true
     */
    @PatchMapping("/{commandId}/ack")
    public ApiResponse<CommandResponse> acknowledge(
            @PathVariable Long commandId,
            @RequestParam Long deviceId,
            @RequestParam(defaultValue = "true") boolean success) {
        return ApiResponse.success(commandService.acknowledge(commandId, deviceId, success));
    }
}
