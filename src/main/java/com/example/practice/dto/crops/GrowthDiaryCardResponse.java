package com.example.practice.dto.crops;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GrowthDiaryCardResponse(
        LocalDate date,
        String thumbnailUrl,
        Integer leafDelta,
        Integer fruitDelta,
        BigDecimal sizeDeltaCm,
        String diseaseName,
        BigDecimal diseaseConfidence
) {
}
