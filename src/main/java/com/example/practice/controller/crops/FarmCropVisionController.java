package com.example.practice.controller.crops;

import com.example.practice.common.config.TokenAuthFilter;
import com.example.practice.dto.crops.VisionInferenceCheckResponse;
import com.example.practice.service.crops.VisionInferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

@Tag(name = "Farm Crop Vision", description = "농장 작물 Vision Inference API")
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/farms")
public class FarmCropVisionController {

    private final VisionInferenceService visionInferenceService;

    @Operation(summary = "작물 이미지 AI 추론", description = "이미지를 AI 서버로 전달해 병해 추론 후 DB에 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추론 및 저장 성공"),
            @ApiResponse(responseCode = "502", description = "AI 서버 응답 오류"),
            @ApiResponse(responseCode = "503", description = "AI 서버 연결 실패"),
            @ApiResponse(responseCode = "504", description = "AI 서버 타임아웃")
    })
    @PostMapping("/{farmId}/crops/{cropsId}/vision-inference")
    public VisionInferenceCheckResponse infer(
            @PathVariable Long farmId,
            @PathVariable Long cropsId,
            @RequestParam("image") MultipartFile image,
            @RequestParam(required = false) Long cameraId,
            @RequestParam(required = false, defaultValue = "DISEASE_CLASSIFICATION") String taskType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime measuredAt,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal user
    ) {
        return visionInferenceService.inferAndSave(
                farmId,
                cropsId,
                user.id(),
                image,
                cameraId,
                taskType,
                measuredAt
        );
    }
}
