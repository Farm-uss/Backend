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
public class GddSchedulerService {

    private final CropsRepository cropsRepository;
    private final GddIngestionService gddIngestionService;

    @Scheduled(cron = "${gdd.scheduler.cron:0 10 3 * * *}")
    public void refreshDailyGdd() {
        List<Crops> crops = cropsRepository.findAllByPlantingDateIsNotNull();
        LocalDate today = LocalDate.now();

        int success = 0;
        int failed = 0;

        for (Crops crop : crops) {
            try {
                gddIngestionService.ensureDailyGddSaved(crop, crop.getPlantingDate(), today);
                success++;
            } catch (Exception e) {
                failed++;
                log.warn("GDD scheduled refresh failed. cropsId={} message={}", crop.getCropsId(), e.getMessage());
            }
        }

        log.info("GDD scheduled refresh finished. total={} success={} failed={}", crops.size(), success, failed);
    }
}
