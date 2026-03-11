package com.example.practice.controller.weather;

import com.example.practice.dto.weather.FarmHourlyWeatherResponseDto;
import com.example.practice.service.weather.FarmWeatherQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/farms")
@SecurityRequirement(name = "JWT")
public class FarmWeatherController {

    private final FarmWeatherQueryService farmWeatherQueryService;

    @Operation(
            summary = "농장 시간별 날씨 조회",
            security = { @SecurityRequirement(name = "JWT") }
    )
    @GetMapping("/{farmId}/weather/hourly")
    public ResponseEntity<FarmHourlyWeatherResponseDto> getHourlyWeather(
            @PathVariable Long farmId
    ) {
        return ResponseEntity.ok(
                farmWeatherQueryService.getHourlyWeather(farmId)
        );
    }
}