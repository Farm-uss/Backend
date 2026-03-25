package com.example.practice.service.schedule;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.schedule.ConditionScheduleResponse;
import com.example.practice.dto.schedule.ScheduleExecutionHistoryResponse;
import com.example.practice.dto.schedule.ScheduleListItemResponse;
import com.example.practice.dto.schedule.TimeScheduleResponse;
import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ScheduleType;
import com.example.practice.repository.schedule.AutomationScheduleRepository;
import com.example.practice.repository.schedule.ScheduleExecutionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleQueryService {

    private final AutomationScheduleRepository scheduleRepository;
    private final ScheduleExecutionHistoryRepository historyRepository;
    private final ScheduleValidator scheduleValidator;

    public List<ScheduleListItemResponse> getByFarmId(Long farmId) {
        scheduleValidator.assertFarmExists(farmId);

        return scheduleRepository.findAllByFarmIdOrderByCreatedAtDesc(farmId).stream()
                .map(ScheduleListItemResponse::from)
                .toList();
    }

    public TimeScheduleResponse getTimeScheduleById(Long scheduleId) {
        AutomationSchedule schedule = getSchedule(scheduleId);

        if (schedule.getScheduleType() != ScheduleType.TIME_BASED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "시간 기반 스케줄이 아닙니다.");
        }

        return TimeScheduleResponse.from(schedule);
    }

    public ConditionScheduleResponse getConditionScheduleById(Long scheduleId) {
        AutomationSchedule schedule = getSchedule(scheduleId);

        if (schedule.getScheduleType() != ScheduleType.CONDITION_BASED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "조건 기반 스케줄이 아닙니다.");
        }

        return ConditionScheduleResponse.from(schedule);
    }

    public List<ScheduleExecutionHistoryResponse> getHistoriesByFarmId(Long farmId) {
        scheduleValidator.assertFarmExists(farmId);

        return historyRepository.findAllBySchedule_FarmIdOrderByExecutedAtDesc(farmId).stream()
                .map(ScheduleExecutionHistoryResponse::from)
                .toList();
    }

    public List<ScheduleExecutionHistoryResponse> getHistoriesByScheduleId(Long scheduleId) {
        getSchedule(scheduleId);

        return historyRepository.findAllBySchedule_ScheduleIdOrderByExecutedAtDesc(scheduleId).stream()
                .map(ScheduleExecutionHistoryResponse::from)
                .toList();
    }

    private AutomationSchedule getSchedule(Long scheduleId) {
        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "스케줄을 찾을 수 없습니다."));
    }
}
