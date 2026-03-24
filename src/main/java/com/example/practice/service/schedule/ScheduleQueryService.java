// src/main/java/com/example/practice/service/schedule/ScheduleQueryService.java
package com.example.practice.service.schedule;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.schedule.ScheduleExecutionHistoryResponse;
import com.example.practice.dto.schedule.ScheduleResponse;
import com.example.practice.entity.schedule.AutomationSchedule;
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

    public List<ScheduleResponse> getByFarmId(Long farmId) {
        scheduleValidator.assertFarmExists(farmId);

        return scheduleRepository.findAllByFarmIdOrderByCreatedAtDesc(farmId).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    public ScheduleResponse getById(Long scheduleId) {
        return ScheduleResponse.from(getSchedule(scheduleId));
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
