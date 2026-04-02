package com.example.practice.service.schedule;

import com.example.practice.dto.farm.CameraCaptureResponse;
import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ControlSystemType;
import com.example.practice.entity.schedule.ExecutionStatus;
import com.example.practice.entity.schedule.ScheduleExecutionHistory;
import com.example.practice.entity.schedule.ScheduleType;
import com.example.practice.entity.schedule.TimeScheduleRule;
import com.example.practice.repository.schedule.AutomationScheduleRepository;
import com.example.practice.repository.schedule.ScheduleExecutionHistoryRepository;
import com.example.practice.service.farm.FarmCameraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CameraScheduleExecutorService {

    private final AutomationScheduleRepository automationScheduleRepository;
    private final ScheduleExecutionHistoryRepository scheduleExecutionHistoryRepository;
    private final FarmCameraService farmCameraService;

    @Value("${camera.scheduler.zone-id:Asia/Seoul}")
    private String zoneId;

    @Scheduled(cron = "${camera.scheduler.cron:0 * * * * *}")
    @Transactional
    public void executeDueCameraSchedules() {
        ZoneId schedulerZone = ZoneId.of(zoneId);
        LocalDateTime now = LocalDateTime.now(schedulerZone).withSecond(0).withNano(0);
        DayOfWeek today = now.getDayOfWeek();

        List<AutomationSchedule> schedules = automationScheduleRepository
                .findAllByEnabledTrueAndScheduleTypeOrderByCreatedAtAsc(ScheduleType.TIME_BASED);

        for (AutomationSchedule schedule : schedules) {
            if (schedule.getControlSystemType() != ControlSystemType.CAMERA) {
                continue;
            }

            TimeScheduleRule timeRule = schedule.getTimeRule();
            if (timeRule == null) {
                continue;
            }
            if (!timeRule.getDayOfWeekValues().contains(today)) {
                continue;
            }
            if (!timeRule.getExecuteTime().equals(now.toLocalTime())) {
                continue;
            }
            if (alreadyExecutedToday(schedule, now.toLocalDate(), schedulerZone)) {
                continue;
            }

            executeSchedule(schedule, schedulerZone);
        }
    }

    private boolean alreadyExecutedToday(AutomationSchedule schedule, LocalDate today, ZoneId schedulerZone) {
        if (schedule.getLastExecutedAt() == null || schedule.getLastExecutionStatus() != ExecutionStatus.SUCCESS) {
            return false;
        }
        return schedule.getLastExecutedAt().atZoneSameInstant(schedulerZone).toLocalDate().isEqual(today);
    }

    private void executeSchedule(AutomationSchedule schedule, ZoneId schedulerZone) {
        OffsetDateTime executedAt = OffsetDateTime.now(schedulerZone);

        try {
            CameraCaptureResponse capture = farmCameraService.captureFarmCameraForSchedule(schedule.getFarmId(), null);
            schedule.updateLastExecution(executedAt, ExecutionStatus.SUCCESS);
            scheduleExecutionHistoryRepository.save(ScheduleExecutionHistory.create(
                    schedule,
                    executedAt.withOffsetSameInstant(ZoneOffset.UTC),
                    ExecutionStatus.SUCCESS,
                    "camera capture success: captureId=" + capture.captureId(),
                    null,
                    schedule.getTimeRule() != null ? schedule.getTimeRule().getDurationMinutes() : null
            ));
        } catch (Exception ex) {
            log.warn("Camera schedule execution failed. scheduleId={} farmId={} message={}",
                    schedule.getScheduleId(), schedule.getFarmId(), ex.getMessage());
            schedule.updateLastExecution(executedAt, ExecutionStatus.ERROR);
            scheduleExecutionHistoryRepository.save(ScheduleExecutionHistory.create(
                    schedule,
                    executedAt.withOffsetSameInstant(ZoneOffset.UTC),
                    ExecutionStatus.ERROR,
                    ex.getMessage(),
                    null,
                    schedule.getTimeRule() != null ? schedule.getTimeRule().getDurationMinutes() : null
            ));
        }
    }
}
