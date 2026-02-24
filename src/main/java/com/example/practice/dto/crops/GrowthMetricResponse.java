package com.example.practice.dto.crops;

import com.example.practice.entity.crops.GrowthMetricSource;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GrowthMetricResponse(
        LocalDate date,
        BigDecimal value,
        GrowthMetricSource source,
        Long imageId
) {
}
