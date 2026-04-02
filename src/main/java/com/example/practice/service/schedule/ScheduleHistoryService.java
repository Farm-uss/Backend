package com.example.practice.service.schedule;

import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ExecutionStatus;
import com.example.practice.entity.schedule.ScheduleExecutionHistory;
import com.example.practice.repository.schedule.ScheduleExecutionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleHistoryService {

    private final ScheduleExecutionHistoryRepository historyRepository;

    public void save(AutomationSchedule schedule,
                     OffsetDateTime executedAt,
                     ExecutionStatus status,
                     String message,
                     BigDecimal triggerValue,
                     Integer durationMinutes) {
        ScheduleExecutionHistory history = ScheduleExecutionHistory.create(
                schedule,
                executedAt,
                status,
                message,
                triggerValue,
                durationMinutes
        );

        historyRepository.save(history);
        schedule.updateLastExecution(executedAt, status);
    }
}
