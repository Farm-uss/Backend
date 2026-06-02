package com.example.practice.dto.crops;

public enum CareTime {
    UNDER_FIVE_MINUTES("5분 이내 (최소 관리)"),
    AROUND_FIFTEEN_MINUTES("15분 내외 (적정 관리)"),
    OVER_THIRTY_MINUTES("30분 이상 (집중 관리)"),
    ONCE_OR_TWICE_A_WEEK("주 1~2회 (비정기 관리)");

    private final String label;

    CareTime(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
