// src/main/java/com/example/practice/entity/schedule/ScheduleExecutionHistory.java
package com.example.practice.entity.schedule;

import com.example.practice.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "schedule_execution_history",
        indexes = {
                @Index(name = "idx_schedule_history_schedule_id", columnList = "schedule_id"),
                @Index(name = "idx_schedule_history_executed_at", columnList = "executed_at")
        }
)
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleExecutionHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private AutomationSchedule schedule;

    @Column(name = "executed_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime executedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ExecutionStatus status;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "trigger_value", precision = 12, scale = 4)
    private BigDecimal triggerValue;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    public static ScheduleExecutionHistory create(AutomationSchedule schedule,
                                                  OffsetDateTime executedAt,
                                                  ExecutionStatus status,
                                                  String message,
                                                  BigDecimal triggerValue,
                                                  Integer durationMinutes) {
        return ScheduleExecutionHistory.builder()
                .schedule(schedule)
                .executedAt(executedAt)
                .status(status)
                .message(message)
                .triggerValue(triggerValue)
                .durationMinutes(durationMinutes)
                .build();
    }
}
