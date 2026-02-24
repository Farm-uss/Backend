package com.example.practice.dto.crops;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GrowthDiaryDetailResponse(
        LocalDate date,
        String imageUrl,
        Temperature temperature,
        Growth growth,
        Gdd gdd,
        Disease disease
) {
    public record Temperature(BigDecimal min, BigDecimal max, BigDecimal avg) {
    }

    public record Growth(Integer leafCount, Integer fruitCount, BigDecimal sizeCm) {
    }

    public record Gdd(BigDecimal daily, BigDecimal cumulative) {
    }

    public record Disease(String name, BigDecimal confidence) {
    }
}
