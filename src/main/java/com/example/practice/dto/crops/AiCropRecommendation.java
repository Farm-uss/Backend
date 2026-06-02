package com.example.practice.dto.crops;

public record AiCropRecommendation(
        int rank,
        String cropName,
        String reason,
        Difficulty difficulty,
        String harvestCycle,
        Integer estimatedHarvestDays
) {
}
