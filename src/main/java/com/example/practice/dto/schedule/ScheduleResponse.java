package com.example.practice.dto.schedule;

import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ControlSystemType;
import com.example.practice.entity.schedule.ExecutionStatus;
import com.example.practice.entity.schedule.ScheduleType;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Getter
@Builder
public class ScheduleResponse {
    private Long scheduleId;
    private Long farmId;
    private String name;
    private ControlSystemType controlSystemType;
    private String controlSystemDescription;
    private ScheduleType scheduleType;
    private String scheduleTypeDescription;
    private boolean enabled;
    private String summary;
    private TimeRuleResponse timeRule;
    private ConditionRuleResponse conditionRule;
    private OffsetDateTime lastExecutedAt;
    private ExecutionStatus lastExecutionStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static ScheduleResponse from(AutomationSchedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .farmId(schedule.getFarmId())
                .name(schedule.getName())
                .controlSystemType(schedule.getControlSystemType())
                .controlSystemDescription(schedule.getControlSystemType().getDescription())
                .scheduleType(schedule.getScheduleType())
                .scheduleTypeDescription(schedule.getScheduleType().getDescription())
                .enabled(schedule.isEnabled())
                .summary(buildSummary(schedule))
                .timeRule(TimeRuleResponse.from(schedule.getTimeRule()))
                .conditionRule(ConditionRuleResponse.from(schedule.getConditionRule()))
                .lastExecutedAt(schedule.getLastExecutedAt())
                .lastExecutionStatus(schedule.getLastExecutionStatus())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    private static String buildSummary(AutomationSchedule schedule) {
        if (schedule.getScheduleType() == ScheduleType.TIME_BASED && schedule.getTimeRule() != null) {
            String days = schedule.getTimeRule().getDayOfWeekValues().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            return days + " " + schedule.getTimeRule().getExecuteTime()
                    + " / " + schedule.getTimeRule().getDurationMinutes() + "분 작동";
        }

        if (schedule.getScheduleType() == ScheduleType.CONDITION_BASED && schedule.getConditionRule() != null) {
            return schedule.getConditionRule().getSensorType().name()
                    + " " + schedule.getConditionRule().getOperator().getDescription()
                    + " " + schedule.getConditionRule().getThresholdValue()
                    + " 일 때 자동 실행";
        }

        return null;
    }
}
