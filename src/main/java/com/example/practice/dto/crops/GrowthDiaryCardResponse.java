package com.example.practice.dto.crops;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GrowthDiaryCardResponse(
        int month,
        int day,
        LocalDate date,
        BigDecimal gdd
) {
}
