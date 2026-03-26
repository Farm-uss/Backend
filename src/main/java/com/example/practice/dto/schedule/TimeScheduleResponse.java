// src/main/java/com/example/practice/dto/schedule/TimeScheduleResponse.java
package com.example.practice.dto.schedule;

import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ControlSystemType;
import com.example.practice.entity.schedule.ExecutionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class TimeScheduleResponse {
    private Long scheduleId;
    private Long farmId;
    private String name;
    private ControlSystemType controlSystemType;
    private String controlSystemDescription;
    private boolean enabled;
    private String summary;
    private TimeRuleResponse timeRule;
    private OffsetDateTime lastExecutedAt;
    private ExecutionStatus lastExecutionStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static TimeScheduleResponse from(AutomationSchedule schedule) {
        return TimeScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .farmId(schedule.getFarmId())
                .name(schedule.getName())
                .controlSystemType(schedule.getControlSystemType())
                .controlSystemDescription(schedule.getControlSystemType().getDescription())
                .enabled(schedule.isEnabled())
                .summary(buildSummary(schedule))
                .timeRule(TimeRuleResponse.from(schedule.getTimeRule()))
                .lastExecutedAt(schedule.getLastExecutedAt())
                .lastExecutionStatus(schedule.getLastExecutionStatus())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    private static String buildSummary(AutomationSchedule schedule) {
        if (schedule.getTimeRule() == null) {
            return null;
        }

        return schedule.getTimeRule().getDayOfWeekValues()
                + " " + schedule.getTimeRule().getExecuteTime()
                + " / " + schedule.getTimeRule().getDurationMinutes() + "분 작동";
    }
}
