// src/main/java/com/example/practice/entity/schedule/AutomationSchedule.java
package com.example.practice.entity.schedule;

import com.example.practice.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "automation_schedule",
        indexes = {
                @Index(name = "idx_schedule_farm_id", columnList = "farm_id"),
                @Index(name = "idx_schedule_enabled", columnList = "enabled")
        }
)
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutomationSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "control_system_type", nullable = false, length = 50)
    private ControlSystemType controlSystemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 50)
    private ScheduleType scheduleType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "last_executed_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastExecutedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_execution_status", length = 50)
    private ExecutionStatus lastExecutionStatus;

    @OneToOne(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TimeScheduleRule timeRule;

    @OneToOne(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ConditionScheduleRule conditionRule;

    public static AutomationSchedule create(Long farmId,
                                            String name,
                                            ControlSystemType controlSystemType,
                                            ScheduleType scheduleType) {
        return AutomationSchedule.builder()
                .farmId(farmId)
                .name(name)
                .controlSystemType(controlSystemType)
                .scheduleType(scheduleType)
                .enabled(true)
                .build();
    }

    public void update(String name,
                       ControlSystemType controlSystemType,
                       ScheduleType scheduleType) {
        this.name = name;
        this.controlSystemType = controlSystemType;
        this.scheduleType = scheduleType;
    }

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void updateLastExecution(OffsetDateTime executedAt, ExecutionStatus status) {
        this.lastExecutedAt = executedAt;
        this.lastExecutionStatus = status;
    }

    public void assignTimeRule(TimeScheduleRule timeRule) {
        this.timeRule = timeRule;
        if (timeRule != null) {
            timeRule.assignSchedule(this);
        }
    }

    public void assignConditionRule(ConditionScheduleRule conditionRule) {
        this.conditionRule = conditionRule;
        if (conditionRule != null) {
            conditionRule.assignSchedule(this);
        }
    }
}
