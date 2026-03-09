package com.example.practice.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardResponseDto {

    private SensorStatus sensorStatus;
    private String sensorMessage;

    private DashboardMetricDto temperature;
    private DashboardMetricDto ph;
    private DashboardMetricDto soilMoisture;
    private DashboardMetricDto co2;
    private DashboardMetricDto ec;
    private DashboardMetricDto light;
}