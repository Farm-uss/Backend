package com.example.practice.service.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulePollingScheduler {

    private final ScheduleExecutionService scheduleExecutionService;

    @Scheduled(cron = "0 * * * * *")
    public void pollSchedules() {
        log.debug("schedule polling start");
        scheduleExecutionService.executeDueSchedules();
    }
}
