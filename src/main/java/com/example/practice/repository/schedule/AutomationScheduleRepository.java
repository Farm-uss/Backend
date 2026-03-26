package com.example.practice.repository.schedule;

import com.example.practice.entity.device.SensorType;
import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ConditionOperator;
import com.example.practice.entity.schedule.ControlSystemType;
import com.example.practice.entity.schedule.ScheduleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AutomationScheduleRepository extends JpaRepository<AutomationSchedule, Long> {

    @EntityGraph(attributePaths = {"timeRule", "conditionRule"})
    List<AutomationSchedule> findAllByFarmIdOrderByCreatedAtDesc(Long farmId);

    @EntityGraph(attributePaths = {"timeRule", "conditionRule"})
    Optional<AutomationSchedule> findByScheduleId(Long scheduleId);

    @Query("""
            select case when count(s) > 0 then true else false end
            from AutomationSchedule s
            join s.timeRule tr
            where s.farmId = :farmId
              and s.controlSystemType = :controlSystemType
              and s.scheduleType = :scheduleType
              and tr.executeTime = :executeTime
              and tr.daysOfWeek = :daysOfWeek
              and tr.durationMinutes = :durationMinutes
            """)
    boolean existsDuplicateTimeSchedule(
            @Param("farmId") Long farmId,
            @Param("controlSystemType") ControlSystemType controlSystemType,
            @Param("scheduleType") ScheduleType scheduleType,
            @Param("executeTime") LocalTime executeTime,
            @Param("daysOfWeek") String daysOfWeek,
            @Param("durationMinutes") Integer durationMinutes
    );

    @Query("""
            select case when count(s) > 0 then true else false end
            from AutomationSchedule s
            join s.timeRule tr
            where s.scheduleId <> :scheduleId
              and s.farmId = :farmId
              and s.controlSystemType = :controlSystemType
              and s.scheduleType = :scheduleType
              and tr.executeTime = :executeTime
              and tr.daysOfWeek = :daysOfWeek
              and tr.durationMinutes = :durationMinutes
            """)
    boolean existsDuplicateTimeScheduleForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("farmId") Long farmId,
            @Param("controlSystemType") ControlSystemType controlSystemType,
            @Param("scheduleType") ScheduleType scheduleType,
            @Param("executeTime") LocalTime executeTime,
            @Param("daysOfWeek") String daysOfWeek,
            @Param("durationMinutes") Integer durationMinutes
    );

    @Query("""
            select case when count(s) > 0 then true else false end
            from AutomationSchedule s
            join s.conditionRule cr
            where s.farmId = :farmId
              and s.controlSystemType = :controlSystemType
              and s.scheduleType = :scheduleType
              and cr.sensorType = :sensorType
              and cr.operator = :operator
              and cr.thresholdValue = :thresholdValue
              and cr.autoStopWhenRecovered = :autoStopWhenRecovered
            """)
    boolean existsDuplicateConditionSchedule(
            @Param("farmId") Long farmId,
            @Param("controlSystemType") ControlSystemType controlSystemType,
            @Param("scheduleType") ScheduleType scheduleType,
            @Param("sensorType") SensorType sensorType,
            @Param("operator") ConditionOperator operator,
            @Param("thresholdValue") BigDecimal thresholdValue,
            @Param("autoStopWhenRecovered") boolean autoStopWhenRecovered
    );

    @Query("""
            select case when count(s) > 0 then true else false end
            from AutomationSchedule s
            join s.conditionRule cr
            where s.scheduleId <> :scheduleId
              and s.farmId = :farmId
              and s.controlSystemType = :controlSystemType
              and s.scheduleType = :scheduleType
              and cr.sensorType = :sensorType
              and cr.operator = :operator
              and cr.thresholdValue = :thresholdValue
              and cr.autoStopWhenRecovered = :autoStopWhenRecovered
            """)
    boolean existsDuplicateConditionScheduleForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("farmId") Long farmId,
            @Param("controlSystemType") ControlSystemType controlSystemType,
            @Param("scheduleType") ScheduleType scheduleType,
            @Param("sensorType") SensorType sensorType,
            @Param("operator") ConditionOperator operator,
            @Param("thresholdValue") BigDecimal thresholdValue,
            @Param("autoStopWhenRecovered") boolean autoStopWhenRecovered
    );
}
