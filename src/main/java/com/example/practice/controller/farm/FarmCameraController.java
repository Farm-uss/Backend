package com.example.practice.controller.farm;

import com.example.practice.common.config.TokenAuthFilter;
import com.example.practice.dto.farm.CameraStreamResponse;
import com.example.practice.service.farm.FarmCameraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Farm Camera", description = "농장 카메라 스트리밍 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/farms")
public class FarmCameraController {

    private final FarmCameraService farmCameraService;

    @Operation(summary = "카메라 스트리밍 URL 조회", description = "farm에 연결된 카메라 스트리밍 URL을 반환합니다.")
    @GetMapping("/{farmId}/camera/stream")
    public CameraStreamResponse getStream(
            @PathVariable Long farmId,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal user
    ) {
        return farmCameraService.getFarmCameraStream(farmId, user.id());
    }
}
