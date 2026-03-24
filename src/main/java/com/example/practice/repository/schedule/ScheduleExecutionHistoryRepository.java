// src/main/java/com/example/practice/repository/schedule/ScheduleExecutionHistoryRepository.java
package com.example.practice.repository.schedule;

import com.example.practice.entity.schedule.ScheduleExecutionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleExecutionHistoryRepository extends JpaRepository<ScheduleExecutionHistory, Long> {

    List<ScheduleExecutionHistory> findAllBySchedule_ScheduleIdOrderByExecutedAtDesc(Long scheduleId);

    List<ScheduleExecutionHistory> findAllBySchedule_FarmIdOrderByExecutedAtDesc(Long farmId);
}
