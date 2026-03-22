// src/main/java/com/example/practice/repository/schedule/AutomationScheduleRepository.java
package com.example.practice.repository.schedule;

import com.example.practice.entity.schedule.AutomationSchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutomationScheduleRepository extends JpaRepository<AutomationSchedule, Long> {

    @EntityGraph(attributePaths = {"timeRule", "conditionRule"})
    List<AutomationSchedule> findAllByFarmIdOrderByCreatedAtDesc(Long farmId);

    @EntityGraph(attributePaths = {"timeRule", "conditionRule"})
    Optional<AutomationSchedule> findByScheduleId(Long scheduleId);
}
