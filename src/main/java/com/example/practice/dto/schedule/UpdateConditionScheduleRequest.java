// src/main/java/com/example/practice/dto/schedule/UpdateConditionScheduleRequest.java
package com.example.practice.dto.schedule;

import com.example.practice.entity.device.SensorType;
import com.example.practice.entity.schedule.ConditionOperator;
import com.example.practice.entity.schedule.ControlSystemType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class UpdateConditionScheduleRequest {
    private Long farmId;
    private String name;
    private ControlSystemType controlSystemType;
    private SensorType sensorType;
    private ConditionOperator operator;
    private BigDecimal thresholdValue;
    private Integer durationMinutes;
    private Boolean autoStopWhenRecovered;
}
