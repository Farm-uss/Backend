package com.example.practice.dto.crops;

public enum HarvestCycle {
    SHORT_TERM("단기 재배 (4주 이내)"),
    MID_TERM("중기 재배 (4주 ~ 12주)"),
    LONG_TERM("장기 재배 (12주 이상)"),
    CONTINUOUS("지속 수확 (상시 수확)");

    private final String label;

    HarvestCycle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
