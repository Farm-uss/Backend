package com.example.practice.dto.crops;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GddTimeSeriesResponse(
        LocalDate date,
        BigDecimal gddDaily,
        BigDecimal gddCumulative
) {}