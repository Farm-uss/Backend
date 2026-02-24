package com.example.practice.dto.crops;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GddSummaryResponse(
        Long farmId,
        Long cropsId,
        Integer targetDays,
        Integer currentDays,
        LocalDate expectedHarvestDate,
        BigDecimal targetGdd,
        BigDecimal currentGdd
) {
}
