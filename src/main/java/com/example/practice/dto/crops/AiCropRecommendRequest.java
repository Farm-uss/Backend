package com.example.practice.dto.crops;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AiCropRecommendRequest(
        @NotBlank(message = "location is required")
        String location,
        @NotNull(message = "place is required")
        GrowPlace place,
        @NotNull(message = "careTime is required")
        CareTime careTime,
        @NotNull(message = "purpose is required")
        GrowPurpose purpose,
        @NotNull(message = "harvestCycle is required")
        HarvestCycle harvestCycle
) {
}
