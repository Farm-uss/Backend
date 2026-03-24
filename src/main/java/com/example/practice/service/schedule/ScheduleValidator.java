package com.example.practice.service.schedule;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.schedule.CreateConditionScheduleRequest;
import com.example.practice.dto.schedule.CreateTimeScheduleRequest;
import com.example.practice.dto.schedule.ScheduleEnabledUpdateRequest;
import com.example.practice.dto.schedule.UpdateConditionScheduleRequest;
import com.example.practice.dto.schedule.UpdateTimeScheduleRequest;
import com.example.practice.repository.farm.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private final FarmRepository farmRepository;

    public void validateCreateTimeSchedule(CreateTimeScheduleRequest request) {
        validateFarmAndName(request.getFarmId(), request.getName());

        if (request.getControlSystemType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "제어 시스템은 필수입니다.");
        }
        if (request.getExecuteTime() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "실행 시간은 필수입니다.");
        }
        if (request.getDaysOfWeek() == null || request.getDaysOfWeek().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "실행 요일은 최소 1개 이상 필요합니다.");
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "작동 시간은 1분 이상이어야 합니다.");
        }
    }

    public void validateUpdateTimeSchedule(UpdateTimeScheduleRequest request) {
        validateFarmAndName(request.getFarmId(), request.getName());

        if (request.getControlSystemType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "제어 시스템은 필수입니다.");
        }
        if (request.getExecuteTime() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "실행 시간은 필수입니다.");
        }
        if (request.getDaysOfWeek() == null || request.getDaysOfWeek().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "실행 요일은 최소 1개 이상 필요합니다.");
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "작동 시간은 1분 이상이어야 합니다.");
        }
    }

    public void validateCreateConditionSchedule(CreateConditionScheduleRequest request) {
        validateFarmAndName(request.getFarmId(), request.getName());

        if (request.getControlSystemType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "제어 시스템은 필수입니다.");
        }
        if (request.getSensorType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "센서 타입은 필수입니다.");
        }
        if (request.getOperator() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "조건 연산자는 필수입니다.");
        }
        if (request.getThresholdValue() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "임계값은 필수입니다.");
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "작동 시간은 1분 이상이어야 합니다.");
        }
    }

    public void validateUpdateConditionSchedule(UpdateConditionScheduleRequest request) {
        validateFarmAndName(request.getFarmId(), request.getName());

        if (request.getControlSystemType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "제어 시스템은 필수입니다.");
        }
        if (request.getSensorType() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "센서 타입은 필수입니다.");
        }
        if (request.getOperator() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "조건 연산자는 필수입니다.");
        }
        if (request.getThresholdValue() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "임계값은 필수입니다.");
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "작동 시간은 1분 이상이어야 합니다.");
        }
    }

    public void validateEnabledUpdate(ScheduleEnabledUpdateRequest request) {
        if (request.getEnabled() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "enabled 값은 필수입니다.");
        }
    }

    public void assertFarmExists(Long farmId) {
        if (farmId == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "farmId는 필수입니다.");
        }

        if (!farmRepository.existsById(farmId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "농장을 찾을 수 없습니다.");
        }
    }

    private void validateFarmAndName(Long farmId, String name) {
        assertFarmExists(farmId);

        if (name == null || name.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "스케줄 이름은 필수입니다.");
        }
    }
}
