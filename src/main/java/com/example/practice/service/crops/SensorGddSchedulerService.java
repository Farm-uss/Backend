package com.example.practice.service.crops;

import com.example.practice.entity.crops.Crops;
import com.example.practice.repository.crops.CropsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorGddSchedulerService {

    private final CropsRepository cropsRepository;
    private final SensorGddIngestionService sensorGddIngestionService;

    @Scheduled(cron = "${gdd.scheduler.cron:0 10 0 * * *}")
    public void refreshPreviousDayGdd() {
        LocalDate targetDate = LocalDate.now().minusDays(1);
        List<Crops> crops = cropsRepository.findAllByPlantingDateIsNotNull();

        int success = 0;
        int failed = 0;

        for (Crops crop : crops) {
            try {
                sensorGddIngestionService.upsertOneDay(crop, targetDate);
                success++;
            } catch (Exception e) {
                failed++;
                log.warn("Sensor GDD scheduled refresh failed. cropsId={} targetDate={} message={}",
                        crop.getCropsId(), targetDate, e.getMessage());
            }
        }

        log.info("Sensor GDD scheduled refresh finished. date={} total={} success={} failed={}",
                targetDate, crops.size(), success, failed);
    }
}
