package com.example.practice.controller.dashboard;

import com.example.practice.dto.dashboard.DashboardResponseDto;
import com.example.practice.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/farms")
@SecurityRequirement(name = "JWT")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/{farmId}/dashboard")
    public DashboardResponseDto getDashboard(@PathVariable Long farmId) {
        return dashboardService.getDashboard(farmId);
    }
}