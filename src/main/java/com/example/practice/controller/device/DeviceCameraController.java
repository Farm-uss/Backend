package com.example.practice.controller.device;

import com.example.practice.common.config.TokenAuthFilter;
import com.example.practice.common.error.AppException;
import com.example.practice.dto.farm.CameraCaptureResponse;
import com.example.practice.service.farm.FarmCameraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Device Camera", description = "디바이스 카메라 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/devices")
public class DeviceCameraController {

    private final FarmCameraService farmCameraService;

    @Operation(summary = "디바이스 카메라 캡처", description = "device에 연결된 카메라에서 현재 이미지를 캡처해 저장합니다.")
    @PostMapping("/{deviceId}/camera/capture")
    public CameraCaptureResponse captureByDevice(
            @PathVariable Long deviceId,
            @RequestParam(required = false) Long cameraId,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal user
    ) {
        if (user == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "authentication is required");
        }
        return farmCameraService.captureDeviceCamera(deviceId, cameraId, user.id());
    }
}
