package com.example.practice.entity.device;
public enum DeviceStatus {

    ACTIVE("정상"),
    INACTIVE("고장");

    private final String description;

    DeviceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
