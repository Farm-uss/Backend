// src/main/java/com/example/practice/entity/schedule/ControlSystemType.java
package com.example.practice.entity.schedule;

public enum ControlSystemType {
    CAMERA("카메라"),
    IRRIGATION("관수 시스템"),
    LIGHTING("조명 시스템"),
    VENTILATION("환기 시스템"),
    HEATING("난방 시스템");

    private final String description;

    ControlSystemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
