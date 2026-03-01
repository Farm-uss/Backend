package com.example.practice.controller.crops;

import com.example.practice.dto.crops.CropStandardResponse;
import com.example.practice.service.crops.CropStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Crop Standard", description = "작물 기준값 조회 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crop-standards")
public class CropStandardController {

    private final CropStandardService cropStandardService;

    @Operation(summary = "작물 기준값 목록 조회", description = "작물 코드/이름/기준 온도/목표 GDD를 반환합니다.")
    @GetMapping
    public List<CropStandardResponse> getCropStandards() {
        return cropStandardService.getAllStandards();
    }
}

