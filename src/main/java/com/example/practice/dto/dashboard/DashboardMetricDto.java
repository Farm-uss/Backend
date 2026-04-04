package com.example.practice.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardMetricDto {
    private String label;
    private String unit;
    private Double min;
    private Double max;
}