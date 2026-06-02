package com.example.practice.dto.crops;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GrowthDiaryDetailResponse(
        String farmName,
        LocalDate date,
        String imageUrl,
        Temperature temperature,
        Growth growth,
        Gdd gdd,
        Disease disease
) {
    public record Temperature(BigDecimal min, BigDecimal max, BigDecimal avg) {
    }

    public record Growth(BigDecimal sizeCm, Integer leafCount, Integer fruitCount) {
    }

    public record Gdd(BigDecimal daily, BigDecimal cumulative) {
    }

    public record Disease(String status, String name, BigDecimal confidence) {
    }
}
