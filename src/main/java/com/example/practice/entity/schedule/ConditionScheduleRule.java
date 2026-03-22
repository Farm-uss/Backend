// src/main/java/com/example/practice/entity/schedule/ConditionScheduleRule.java
package com.example.practice.entity.schedule;

import com.example.practice.entity.device.SensorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "condition_schedule_rule")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConditionScheduleRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false, unique = true)
    private AutomationSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 50)
    private SensorType sensorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_operator", nullable = false, length = 50)
    private ConditionOperator operator;

    @Column(name = "threshold_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "auto_stop_when_recovered", nullable = false)
    private boolean autoStopWhenRecovered;

    public static ConditionScheduleRule create(SensorType sensorType,
                                               ConditionOperator operator,
                                               BigDecimal thresholdValue,
                                               Integer durationMinutes,
                                               boolean autoStopWhenRecovered) {
        return ConditionScheduleRule.builder()
                .sensorType(sensorType)
                .operator(operator)
                .thresholdValue(thresholdValue)
                .durationMinutes(durationMinutes)
                .autoStopWhenRecovered(autoStopWhenRecovered)
                .build();
    }

    public void update(SensorType sensorType,
                       ConditionOperator operator,
                       BigDecimal thresholdValue,
                       Integer durationMinutes,
                       boolean autoStopWhenRecovered) {
        this.sensorType = sensorType;
        this.operator = operator;
        this.thresholdValue = thresholdValue;
        this.durationMinutes = durationMinutes;
        this.autoStopWhenRecovered = autoStopWhenRecovered;
    }

    void assignSchedule(AutomationSchedule schedule) {
        this.schedule = schedule;
    }
}
