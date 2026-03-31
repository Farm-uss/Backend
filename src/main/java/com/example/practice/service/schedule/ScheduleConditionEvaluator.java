package com.example.practice.service.schedule;

import com.example.practice.entity.schedule.ConditionOperator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ScheduleConditionEvaluator {

    public boolean matches(BigDecimal sensorValue, ConditionOperator operator, BigDecimal thresholdValue) {
        if (sensorValue == null || operator == null || thresholdValue == null) {
            return false;
        }

        int compare = sensorValue.compareTo(thresholdValue);

        return switch (operator) {
            case GREATER_THAN -> compare > 0;
            case GREATER_THAN_OR_EQUAL -> compare >= 0;
            case LESS_THAN -> compare < 0;
            case LESS_THAN_OR_EQUAL -> compare <= 0;
        };
    }
}
