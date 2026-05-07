package com.example.practice.dto.crops;

public enum GrowPlace {
    INDOOR_WINDOW_DESK("실내 창가/책상"),
    APARTMENT_BALCONY("아파트 베란다"),
    ROOFTOP_YARD_GARDEN("옥상/마당/텃밭"),
    SMART_FARM_LIGHTING("조명 시설이 갖춰진 스마트팜");

    private final String label;

    GrowPlace(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
