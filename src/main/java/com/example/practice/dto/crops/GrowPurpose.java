package com.example.practice.dto.crops;

public enum GrowPurpose {
    PRACTICAL_FOOD("실용적인 식재료"),
    HEALING_AESTHETIC("심미적 힐링"),
    EDUCATION_OBSERVATION("아이들 교육/성장 관찰"),
    COST_EFFECTIVE("최고의 가성비");

    private final String label;

    GrowPurpose(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
