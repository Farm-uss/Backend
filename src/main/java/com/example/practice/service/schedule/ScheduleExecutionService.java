package com.example.practice.service.schedule;

import com.example.practice.entity.device.CommandType;
import com.example.practice.entity.device.SensorType;
import com.example.practice.entity.schedule.AutomationSchedule;
import com.example.practice.entity.schedule.ControlSystemType;
import com.example.practice.entity.schedule.ExecutionStatus;
import com.example.practice.entity.schedule.ScheduleType;
import com.example.practice.repository.schedule.AutomationScheduleRepository;
import com.example.practice.service.device.DeviceCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleExecutionService {

    private final AutomationScheduleRepository scheduleRepository;
    private final ScheduleConditionEvaluator scheduleConditionEvaluator;
    private final ScheduleHistoryService scheduleHistoryService;
    private final DeviceCommandService deviceCommandService;
    private final SensorValueQueryService sensorValueQueryService;

    public void executeDueSchedules() {
        List<AutomationSchedule> schedules = scheduleRepository.findAll();

        OffsetDateTime now = OffsetDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime().withSecond(0).withNano(0);

        for (AutomationSchedule schedule : schedules) {
            if (!schedule.isEnabled()) {
                continue;
            }

            if (executedRecently(schedule, now)) {
                continue;
            }

            if (schedule.getScheduleType() == ScheduleType.TIME_BASED) {
                handleTimeBasedSchedule(schedule, now, currentDay, currentTime);
            } else if (schedule.getScheduleType() == ScheduleType.CONDITION_BASED) {
                handleConditionBasedSchedule(schedule, now);
            }
        }
    }

    private void handleTimeBasedSchedule(AutomationSchedule schedule,
                                         OffsetDateTime now,
                                         DayOfWeek currentDay,
                                         LocalTime currentTime) {
        if (schedule.getTimeRule() == null) {
            return;
        }

        boolean dayMatched = schedule.getTimeRule().getDayOfWeekValues().contains(currentDay);
        boolean timeMatched = schedule.getTimeRule().getExecuteTime()
                .withSecond(0)
                .withNano(0)
                .equals(currentTime);

        if (!dayMatched || !timeMatched) {
            return;
        }

        try {
            executeOnCommand(schedule);

            scheduleHistoryService.save(
                    schedule,
                    now,
                    ExecutionStatus.SUCCESS,
                    buildSuccessMessage(schedule, true),
                    null,
                    schedule.getTimeRule().getDurationMinutes()
            );
        } catch (Exception e) {
            scheduleHistoryService.save(
                    schedule,
                    now,
                    ExecutionStatus.ERROR,
                    e.getMessage(),
                    null,
                    schedule.getTimeRule().getDurationMinutes()
            );
        }
    }

    private void handleConditionBasedSchedule(AutomationSchedule schedule, OffsetDateTime now) {
        if (schedule.getConditionRule() == null) {
            return;
        }

        SensorType sensorType = schedule.getConditionRule().getSensorType();
        BigDecimal latestValue = sensorValueQueryService.getLatestFarmSensorValue(
                schedule.getFarmId(),
                sensorType
        );

        if (latestValue == null) {
            scheduleHistoryService.save(
                    schedule,
                    now,
                    ExecutionStatus.ERROR,
                    "조건 기반 스케줄 평가 실패: 센서값이 없습니다.",
                    null,
                    null
            );
            return;
        }

        boolean matched = scheduleConditionEvaluator.matches(
                latestValue,
                schedule.getConditionRule().getOperator(),
                schedule.getConditionRule().getThresholdValue()
        );

        try {
            if (matched) {
                executeOnCommand(schedule);

                scheduleHistoryService.save(
                        schedule,
                        now,
                        ExecutionStatus.SUCCESS,
                        buildSuccessMessage(schedule, false),
                        latestValue,
                        null
                );
            } else if (schedule.getConditionRule().isAutoStopWhenRecovered()) {
                executeOffCommand(schedule);

                scheduleHistoryService.save(
                        schedule,
                        now,
                        ExecutionStatus.AUTO_CANCELLED,
                        buildRecoveredMessage(schedule),
                        latestValue,
                        null
                );
            }
        } catch (Exception e) {
            scheduleHistoryService.save(
                    schedule,
                    now,
                    ExecutionStatus.ERROR,
                    e.getMessage(),
                    latestValue,
                    null
            );
        }
    }

    private void executeOnCommand(AutomationSchedule schedule) {
        switch (schedule.getControlSystemType()) {
            case LIGHTING -> deviceCommandService.createCommand(CommandType.LED_ON);

            case IRRIGATION -> {
                // TODO: 관수 서비스 추가되면 연결
                throw new UnsupportedOperationException("관수 실행 기능이 아직 연결되지 않았습니다.");
            }

            case VENTILATION -> {
                // TODO: 환기 서비스 추가되면 연결
                throw new UnsupportedOperationException("환기 실행 기능이 아직 연결되지 않았습니다.");
            }

            case HEATING -> {
                // TODO: 난방 서비스 추가되면 연결
                throw new UnsupportedOperationException("난방 실행 기능이 아직 연결되지 않았습니다.");
            }
        }
    }

    private void executeOffCommand(AutomationSchedule schedule) {
        switch (schedule.getControlSystemType()) {
            case LIGHTING -> deviceCommandService.createCommand(CommandType.LED_OFF);

            case IRRIGATION -> {
                // TODO: 관수 중지 기능 추가되면 연결
                throw new UnsupportedOperationException("관수 중지 기능이 아직 연결되지 않았습니다.");
            }

            case VENTILATION -> {
                // TODO: 환기 중지 기능 추가되면 연결
                throw new UnsupportedOperationException("환기 중지 기능이 아직 연결되지 않았습니다.");
            }

            case HEATING -> {
                // TODO: 난방 중지 기능 추가되면 연결
                throw new UnsupportedOperationException("난방 중지 기능이 아직 연결되지 않았습니다.");
            }
        }
    }

    private String buildSuccessMessage(AutomationSchedule schedule, boolean timeBased) {
        String prefix = timeBased ? "시간 기반" : "조건 기반";

        return switch (schedule.getControlSystemType()) {
            case LIGHTING -> prefix + " 조명 스케줄 실행 성공";
            case IRRIGATION -> prefix + " 관수 스케줄 실행 성공";
            case VENTILATION -> prefix + " 환기 스케줄 실행 성공";
            case HEATING -> prefix + " 난방 스케줄 실행 성공";
            case CAMERA -> prefix + " 카메라 스케줄 실행";
        };
    }

    private String buildRecoveredMessage(AutomationSchedule schedule) {
        return switch (schedule.getControlSystemType()) {
            case LIGHTING -> "조건 회복으로 조명 OFF 명령 전송";
            case IRRIGATION -> "조건 회복으로 관수 중지 명령 전송";
            case VENTILATION -> "조건 회복으로 환기 중지 명령 전송";
            case HEATING -> "조건 회복으로 난방 중지 명령 전송";
            case CAMERA -> "카메라 제어는 자동 종료를 지원하지 않습니다.";

        };
    }

    private boolean executedRecently(AutomationSchedule schedule, OffsetDateTime now) {
        if (schedule.getLastExecutedAt() == null) {
            return false;
        }
        return schedule.getLastExecutedAt().isAfter(now.minusMinutes(1));
    }
}
