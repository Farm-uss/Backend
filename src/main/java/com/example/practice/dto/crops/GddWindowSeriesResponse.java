package com.example.practice.dto.crops;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GddWindowSeriesResponse(
        LocalDate from,
        LocalDate to,
        Integer windowDays,
        BigDecimal gddSum,
        BigDecimal gddCumulative
) {
}
