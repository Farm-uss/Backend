// src/main/java/com/example/practice/entity/schedule/ExecutionStatus.java
package com.example.practice.entity.schedule;

public enum ExecutionStatus {
    SUCCESS("실행 성공"),
    AUTO_CANCELLED("자동 취소"),
    ERROR("오류");

    private final String description;

    ExecutionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}