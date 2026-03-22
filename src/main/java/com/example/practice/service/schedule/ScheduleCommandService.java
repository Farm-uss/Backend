// src/main/java/com/example/practice/service/schedule/ScheduleCommandService.java
package com.example.practice.service.schedule;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.schedule.ConditionRuleRequest;
import com.example.practice.dto.schedule.ScheduleEnabledUpdateRequest;
import com.example.practice.dto.schedule.ScheduleResponse;
import com.example.practice.dto.schedule.ScheduleUpsertRequest;
import com.example.practice.dto.schedule.TimeRuleRequest;
import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ConditionScheduleRule;
import com.example.practice.entity.schedule.ScheduleType;
import com.example.practice.entity.schedule.TimeScheduleRule;
import com.example.practice.repository.schedule.AutomationScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleCommandService {

    private final AutomationScheduleRepository scheduleRepository;
    private final ScheduleValidator scheduleValidator;
    private final ScheduleMapper scheduleMapper;


    public ScheduleResponse create(ScheduleUpsertRequest request) {
        scheduleValidator.validateCreateOrUpdate(request);

        AutomationSchedule schedule = AutomationSchedule.create(
                request.getFarmId(),
                request.getName(),
                request.getControlSystemType(),
                request.getScheduleType()
        );

        applyRule(schedule, request);

        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    public ScheduleResponse update(Long scheduleId, ScheduleUpsertRequest request) {
        scheduleValidator.validateCreateOrUpdate(request);

        AutomationSchedule schedule = getSchedule(scheduleId);

        if (!schedule.getFarmId().equals(request.getFarmId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "farmId는 변경할 수 없습니다.");
        }

        schedule.update(
                request.getName(),
                request.getControlSystemType(),
                request.getScheduleType()
        );

        if (request.getScheduleType() == ScheduleType.TIME_BASED) {
            updateTimeRule(schedule, request.getTimeRule());
        } else {
            updateConditionRule(schedule, request.getConditionRule());
        }

        return scheduleMapper.toResponse(schedule);
    }

    public ScheduleResponse updateEnabled(Long scheduleId, ScheduleEnabledUpdateRequest request) {
        scheduleValidator.validateEnabledUpdate(request);

        AutomationSchedule schedule = getSchedule(scheduleId);
        schedule.updateEnabled(request.getEnabled());

        return ScheduleResponse.from(schedule);
    }

    public void delete(Long scheduleId) {
        AutomationSchedule schedule = getSchedule(scheduleId);
        scheduleRepository.delete(schedule);
    }

    private void applyRule(AutomationSchedule schedule, ScheduleUpsertRequest request) {
        if (request.getScheduleType() == ScheduleType.TIME_BASED) {
            TimeRuleRequest timeRuleRequest = request.getTimeRule();

            TimeScheduleRule timeRule = TimeScheduleRule.create(
                    timeRuleRequest.getExecuteTime(),
                    timeRuleRequest.getDaysOfWeek(),
                    timeRuleRequest.getDurationMinutes()
            );

            schedule.assignTimeRule(timeRule);
            return;
        }

        ConditionRuleRequest conditionRuleRequest = request.getConditionRule();

        ConditionScheduleRule conditionRule = ConditionScheduleRule.create(
                conditionRuleRequest.getSensorType(),
                conditionRuleRequest.getOperator(),
                conditionRuleRequest.getThresholdValue(),
                conditionRuleRequest.getDurationMinutes(),
                Boolean.TRUE.equals(conditionRuleRequest.getAutoStopWhenRecovered())
        );

        schedule.assignConditionRule(conditionRule);
    }
    private void updateTimeRule(AutomationSchedule schedule, TimeRuleRequest request) {
        schedule.assignConditionRule(null);

        if (schedule.getTimeRule() != null) {
            schedule.getTimeRule().update(
                    request.getExecuteTime(),
                    request.getDaysOfWeek(),
                    request.getDurationMinutes()
            );
            return;
        }

        TimeScheduleRule timeRule = TimeScheduleRule.create(
                request.getExecuteTime(),
                request.getDaysOfWeek(),
                request.getDurationMinutes()
        );
        schedule.assignTimeRule(timeRule);
    }

    private void updateConditionRule(AutomationSchedule schedule, ConditionRuleRequest request) {
        schedule.assignTimeRule(null);

        if (schedule.getConditionRule() != null) {
            schedule.getConditionRule().update(
                    request.getSensorType(),
                    request.getOperator(),
                    request.getThresholdValue(),
                    request.getDurationMinutes(),
                    Boolean.TRUE.equals(request.getAutoStopWhenRecovered())
            );
            return;
        }

        ConditionScheduleRule conditionRule = ConditionScheduleRule.create(
                request.getSensorType(),
                request.getOperator(),
                request.getThresholdValue(),
                request.getDurationMinutes(),
                Boolean.TRUE.equals(request.getAutoStopWhenRecovered())
        );
        schedule.assignConditionRule(conditionRule);
    }


    private AutomationSchedule getSchedule(Long scheduleId) {
        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "스케줄을 찾을 수 없습니다."));
    }
}
