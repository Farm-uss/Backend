// src/main/java/com/example/practice/service/schedule/ScheduleMapper.java
package com.example.practice.service.schedule;

import com.example.practice.dto.schedule.ConditionRuleResponse;
import com.example.practice.dto.schedule.ScheduleExecutionHistoryResponse;
import com.example.practice.dto.schedule.ScheduleResponse;
import com.example.practice.dto.schedule.TimeRuleResponse;
import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ScheduleExecutionHistory;
import com.example.practice.entity.schedule.ScheduleType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ScheduleMapper {

    public ScheduleResponse toResponse(AutomationSchedule schedule) {
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

    public ScheduleExecutionHistoryResponse toHistoryResponse(ScheduleExecutionHistory history) {
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

    private String buildSummary(AutomationSchedule schedule) {
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
