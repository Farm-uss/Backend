// src/main/java/com/example/practice/dto/schedule/ScheduleExecutionHistoryResponse.java
package com.example.practice.dto.schedule;

import com.example.practice.entity.schedule.ExecutionStatus;
import com.example.practice.entity.schedule.ScheduleExecutionHistory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class ScheduleExecutionHistoryResponse {
    private Long historyId;
    private Long scheduleId;
    private String scheduleName;
    private OffsetDateTime executedAt;
    private ExecutionStatus status;
    private String statusDescription;
    private String message;
    private BigDecimal triggerValue;
    private Integer durationMinutes;

    public static ScheduleExecutionHistoryResponse from(ScheduleExecutionHistory history) {
        return ScheduleExecutionHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .scheduleId(history.getSchedule().getScheduleId())
                .scheduleName(history.getSchedule().getName())
                .executedAt(history.getExecutedAt())
                .status(history.getStatus())
                .statusDescription(history.getStatus().getDescription())
                .message(history.getMessage())
                .triggerValue(history.getTriggerValue())
                .durationMinutes(history.getDurationMinutes())
                .build();
    }
}
