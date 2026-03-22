// src/main/java/com/example/practice/service/schedule/ScheduleValidator.java
package com.example.practice.service.schedule;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.schedule.ConditionRuleRequest;
import com.example.practice.dto.schedule.ScheduleEnabledUpdateRequest;
import com.example.practice.dto.schedule.ScheduleUpsertRequest;
import com.example.practice.dto.schedule.TimeRuleRequest;
import com.example.practice.entity.schedule.ScheduleType;
import com.example.practice.repository.farm.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private final FarmRepository farmRepository;

    public void validateCreateOrUpdate(ScheduleUpsertRequest request) {
        if (request.getFarmId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "farmId는 필수입니다.");
        }

        assertFarmExists(request.getFarmId());

        if (request.getName() == null || request.getName().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "스케줄 이름은 필수입니다.");
        }

        if (request.getControlSystemType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "제어 시스템은 필수입니다.");
        }

        if (request.getScheduleType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "스케줄 타입은 필수입니다.");
        }

        if (request.getScheduleType() == ScheduleType.TIME_BASED) {
            validateTimeRule(request.getTimeRule());
        } else {
            validateConditionRule(request.getConditionRule());
        }
    }

    public void validateEnabledUpdate(ScheduleEnabledUpdateRequest request) {
        if (request.getEnabled() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "enabled 값은 필수입니다.");
        }
    }

    public void assertFarmExists(Long farmId) {
        if (!farmRepository.existsById(farmId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "농장을 찾을 수 없습니다.");
        }
    }

    private void validateTimeRule(TimeRuleRequest timeRule) {
        if (timeRule == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "시간 기반 규칙이 필요합니다.");
        }

        if (timeRule.getExecuteTime() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "실행 시간은 필수입니다.");
        }

        if (timeRule.getDaysOfWeek() == null || timeRule.getDaysOfWeek().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "실행 요일은 최소 1개 이상 필요합니다.");
        }

        if (timeRule.getDurationMinutes() == null || timeRule.getDurationMinutes() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "작동 시간은 1분 이상이어야 합니다.");
        }
    }

    private void validateConditionRule(ConditionRuleRequest conditionRule) {
        if (conditionRule == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "조건 기반 규칙이 필요합니다.");
        }

        if (conditionRule.getSensorType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "센서 타입은 필수입니다.");
        }

        if (conditionRule.getOperator() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "실행 조건은 필수입니다.");
        }

        if (conditionRule.getThresholdValue() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "실행 조건 값은 필수입니다.");
        }

        if (conditionRule.getDurationMinutes() == null || conditionRule.getDurationMinutes() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "작동 시간은 1분 이상이어야 합니다.");
        }
    }
}
