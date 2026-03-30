package com.example.practice.dto.schedule;

import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ControlSystemType;
import com.example.practice.entity.schedule.ExecutionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ConditionScheduleResponse {
    private Long scheduleId;
    private Long farmId;
    private String name;
    private ControlSystemType controlSystemType;
    private String controlSystemDescription;
    private boolean enabled;
    private String summary;
    private ConditionRuleResponse conditionRule;
    private OffsetDateTime lastExecutedAt;
    private ExecutionStatus lastExecutionStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static ConditionScheduleResponse from(AutomationSchedule schedule) {
        return ConditionScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .farmId(schedule.getFarmId())
                .name(schedule.getName())
                .controlSystemType(schedule.getControlSystemType())
                .controlSystemDescription(schedule.getControlSystemType().getDescription())
                .enabled(schedule.isEnabled())
                .summary(buildSummary(schedule))
                .conditionRule(ConditionRuleResponse.from(schedule.getConditionRule()))
                .lastExecutedAt(schedule.getLastExecutedAt())
                .lastExecutionStatus(schedule.getLastExecutionStatus())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    private static String buildSummary(AutomationSchedule schedule) {
        if (schedule.getConditionRule() == null) {
            return null;
        }

        return schedule.getConditionRule().getSensorType().name()
                + " " + schedule.getConditionRule().getOperator().getDescription()
                + " " + schedule.getConditionRule().getThresholdValue()
                + " 일 때 자동 실행";
    }
}
