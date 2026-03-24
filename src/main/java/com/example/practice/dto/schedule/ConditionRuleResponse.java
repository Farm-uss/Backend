// src/main/java/com/example/practice/dto/schedule/ConditionRuleResponse.java
package com.example.practice.dto.schedule;

import com.example.practice.entity.device.SensorType;
import com.example.practice.entity.schedule.ConditionOperator;
import com.example.practice.entity.schedule.ConditionScheduleRule;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ConditionRuleResponse {
    private SensorType sensorType;
    private ConditionOperator operator;
    private BigDecimal thresholdValue;
    private Integer durationMinutes;
    private boolean autoStopWhenRecovered;

    public static ConditionRuleResponse from(ConditionScheduleRule rule) {
        if (rule == null) {
            return null;
        }

        return ConditionRuleResponse.builder()
                .sensorType(rule.getSensorType())
                .operator(rule.getOperator())
                .thresholdValue(rule.getThresholdValue())
                .durationMinutes(rule.getDurationMinutes())
                .autoStopWhenRecovered(rule.isAutoStopWhenRecovered())
                .build();
    }
}
