package com.example.practice.entity.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    TEMP_HIGH("온도 초과"), TEMP_LOW("온도 부족"),
    CO2_HIGH("CO2 초과"),  CO2_LOW("CO2 부족"),
    EC_HIGH("EC 초과"),    EC_LOW("EC 부족"),
    LIGHT_HIGH("조도 초과"), LIGHT_LOW("조도 부족"),
    SOIL_MOISTURE_HIGH("토양수분 초과"), SOIL_MOISTURE_LOW("토양수분 부족"),
    PH_HIGH("pH 초과"),    PH_LOW("pH 부족");

    private final String description;
    NotificationType(String description) { this.description = description; }
}