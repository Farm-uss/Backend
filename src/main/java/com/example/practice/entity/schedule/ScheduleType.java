// src/main/java/com/example/practice/entity/schedule/ScheduleType.java
package com.example.practice.entity.schedule;

public enum ScheduleType {
    TIME_BASED("시간 기반"),
    CONDITION_BASED("조건 기반");

    private final String description;

    ScheduleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
