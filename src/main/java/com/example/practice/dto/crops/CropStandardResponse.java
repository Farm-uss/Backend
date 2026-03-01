package com.example.practice.dto.crops;

import java.math.BigDecimal;

public record CropStandardResponse(
        String cropCode,
        String cropName,
        BigDecimal baseTemp,
        BigDecimal targetGdd
) {
}

