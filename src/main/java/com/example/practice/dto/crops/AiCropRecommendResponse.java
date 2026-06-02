package com.example.practice.dto.crops;

import java.util.List;

public record AiCropRecommendResponse(
        List<AiCropRecommendation> recommendations,
        String message
) {
}
