package com.example.practice.service.schedule;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.schedule.*;
import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ConditionScheduleRule;
import com.example.practice.entity.schedule.ScheduleType;
import com.example.practice.entity.schedule.TimeScheduleRule;
import com.example.practice.repository.schedule.AutomationScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleCommandService {

    private final AutomationScheduleRepository scheduleRepository;
    private final ScheduleValidator scheduleValidator;

    public TimeScheduleResponse createTimeSchedule(CreateTimeScheduleRequest request) {
        scheduleValidator.validateCreateTimeSchedule(request);

        String daysOfWeek = toDaysOfWeekString(request.getDaysOfWeek());

        boolean duplicated = scheduleRepository.existsDuplicateTimeSchedule(
                request.getFarmId(),
                request.getControlSystemType(),
                ScheduleType.TIME_BASED,
                request.getExecuteTime(),
                daysOfWeek,
                request.getDurationMinutes()
        );

        if (duplicated) {
            throw new AppException(HttpStatus.CONFLICT, "동일한 시간 기반 스케줄이 이미 존재합니다.");
        }

        AutomationSchedule schedule = AutomationSchedule.create(
                request.getFarmId(),
                request.getName(),
                request.getControlSystemType(),
                ScheduleType.TIME_BASED
        );

        TimeScheduleRule timeRule = TimeScheduleRule.create(
                request.getExecuteTime(),
                request.getDaysOfWeek(),
                request.getDurationMinutes()
        );
        schedule.assignTimeRule(timeRule);

        return TimeScheduleResponse.from(scheduleRepository.save(schedule));
    }

    public ConditionScheduleResponse createConditionSchedule(CreateConditionScheduleRequest request) {
        scheduleValidator.validateCreateConditionSchedule(request);

        boolean duplicated = scheduleRepository.existsDuplicateConditionSchedule(
                request.getFarmId(),
                request.getControlSystemType(),
                ScheduleType.CONDITION_BASED,
                request.getSensorType(),
                request.getOperator(),
                request.getThresholdValue(),
                request.getDurationMinutes(),
                Boolean.TRUE.equals(request.getAutoStopWhenRecovered())
        );

        if (duplicated) {
            throw new AppException(HttpStatus.CONFLICT, "동일한 조건 기반 스케줄이 이미 존재합니다.");
        }

        AutomationSchedule schedule = AutomationSchedule.create(
                request.getFarmId(),
                request.getName(),
                request.getControlSystemType(),
                ScheduleType.CONDITION_BASED
        );

        ConditionScheduleRule conditionRule = ConditionScheduleRule.create(
                request.getSensorType(),
                request.getOperator(),
                request.getThresholdValue(),
                request.getDurationMinutes(),
                Boolean.TRUE.equals(request.getAutoStopWhenRecovered())
        );
        schedule.assignConditionRule(conditionRule);

        return ConditionScheduleResponse.from(scheduleRepository.save(schedule));
    }

    public TimeScheduleResponse updateTimeSchedule(Long scheduleId, UpdateTimeScheduleRequest request) {
        scheduleValidator.validateUpdateTimeSchedule(request);

        AutomationSchedule schedule = getSchedule(scheduleId);

        if (!schedule.getFarmId().equals(request.getFarmId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "farmId는 변경할 수 없습니다.");
        }

        String daysOfWeek = toDaysOfWeekString(request.getDaysOfWeek());

        boolean duplicated = scheduleRepository.existsDuplicateTimeScheduleForUpdate(
                scheduleId,
                request.getFarmId(),
                request.getControlSystemType(),
                ScheduleType.TIME_BASED,
                request.getExecuteTime(),
                daysOfWeek,
                request.getDurationMinutes()
        );

        if (duplicated) {
            throw new AppException(HttpStatus.CONFLICT, "동일한 시간 기반 스케줄이 이미 존재합니다.");
        }

        schedule.update(
                request.getName(),
                request.getControlSystemType(),
                ScheduleType.TIME_BASED
        );

        schedule.assignConditionRule(null);

        if (schedule.getTimeRule() != null) {
            schedule.getTimeRule().update(
                    request.getExecuteTime(),
                    request.getDaysOfWeek(),
                    request.getDurationMinutes()
            );
        } else {
            TimeScheduleRule timeRule = TimeScheduleRule.create(
                    request.getExecuteTime(),
                    request.getDaysOfWeek(),
                    request.getDurationMinutes()
            );
            schedule.assignTimeRule(timeRule);
        }

        return TimeScheduleResponse.from(schedule);
    }

    public ConditionScheduleResponse updateConditionSchedule(Long scheduleId, UpdateConditionScheduleRequest request) {
        scheduleValidator.validateUpdateConditionSchedule(request);

        AutomationSchedule schedule = getSchedule(scheduleId);

        if (!schedule.getFarmId().equals(request.getFarmId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "farmId는 변경할 수 없습니다.");
        }

        boolean duplicated = scheduleRepository.existsDuplicateConditionScheduleForUpdate(
                scheduleId,
                request.getFarmId(),
                request.getControlSystemType(),
                ScheduleType.CONDITION_BASED,
                request.getSensorType(),
                request.getOperator(),
                request.getThresholdValue(),
                request.getDurationMinutes(),
                Boolean.TRUE.equals(request.getAutoStopWhenRecovered())
        );

        if (duplicated) {
            throw new AppException(HttpStatus.CONFLICT, "동일한 조건 기반 스케줄이 이미 존재합니다.");
        }

        schedule.update(
                request.getName(),
                request.getControlSystemType(),
                ScheduleType.CONDITION_BASED
        );

        schedule.assignTimeRule(null);

        if (schedule.getConditionRule() != null) {
            schedule.getConditionRule().update(
                    request.getSensorType(),
                    request.getOperator(),
                    request.getThresholdValue(),
                    request.getDurationMinutes(),
                    Boolean.TRUE.equals(request.getAutoStopWhenRecovered())
            );
        } else {
            ConditionScheduleRule conditionRule = ConditionScheduleRule.create(
                    request.getSensorType(),
                    request.getOperator(),
                    request.getThresholdValue(),
                    request.getDurationMinutes(),
                    Boolean.TRUE.equals(request.getAutoStopWhenRecovered())
            );
            schedule.assignConditionRule(conditionRule);
        }

        return ConditionScheduleResponse.from(schedule);
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

    private AutomationSchedule getSchedule(Long scheduleId) {
        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "스케줄을 찾을 수 없습니다."));
    }

    private String toDaysOfWeekString(List<DayOfWeek> daysOfWeek) {
        return daysOfWeek.stream()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(","));
    }
}
