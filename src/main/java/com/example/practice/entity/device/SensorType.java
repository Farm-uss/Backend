package com.example.practice.entity.device;

public enum SensorType {

    ILLUMINANCE("조도", "lux"),
    SOIL_TEMPERATURE("토양 온도", "℃"),
    SOIL_HUMIDITY("토양 습도", "%"),
    SOIL_MOISTURE("토양 수분", "%"),
    CO2("Co2", "ppm"),
    EC("EC", "dS/m");

    private final String description;
    private final String defaultUnit;

    SensorType(String description, String defaultUnit) {
        this.description = description;
        this.defaultUnit = defaultUnit;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }
}
