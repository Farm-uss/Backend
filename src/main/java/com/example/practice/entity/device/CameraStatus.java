package com.example.practice.entity.device;

public enum CameraStatus {

    ONLINE("연결됨"),
    OFFLINE("연결 안 됨"),
    ERROR("오류");

    private final String description;

    CameraStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
