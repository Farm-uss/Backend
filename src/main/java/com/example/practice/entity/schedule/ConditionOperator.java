// src/main/java/com/example/practice/entity/schedule/ConditionOperator.java
package com.example.practice.entity.schedule;

public enum ConditionOperator {
    GREATER_THAN("보다 큰"),
    GREATER_THAN_OR_EQUAL("이상"),
    LESS_THAN("보다 작은"),
    LESS_THAN_OR_EQUAL("이하");

    private final String description;

    ConditionOperator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
