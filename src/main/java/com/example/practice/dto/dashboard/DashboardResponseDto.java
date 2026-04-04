package com.example.practice.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardResponseDto {
    private String cropName;
    private String cropCode;
    private DashboardMetricDto temperature;
    private DashboardMetricDto ph;
    private DashboardMetricDto soilMoisture;
    private DashboardMetricDto co2;
    private DashboardMetricDto ec;
    private DashboardMetricDto illuminance;
}