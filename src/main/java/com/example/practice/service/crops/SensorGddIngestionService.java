package com.example.practice.service.crops;

import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.device.SensorType;
import com.example.practice.repository.crops.CropGddDailyRepository;
import com.example.practice.repository.device.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SensorGddIngestionService {

    public static final String SOURCE_SENSOR = "SENSOR";
    public static final String SOURCE_NO_SENSOR_DATA = "NO_SENSOR_DATA";

    private final CropGddDailyRepository cropGddDailyRepository;
    private final SensorReadingRepository sensorReadingRepository;

    @Transactional
    public void ensureDateRangeSaved(Crops crop, LocalDate from, LocalDate to) {
        if (crop == null || from == null || to == null || from.isAfter(to)) {
            return;
        }
        List<LocalDate> saved = cropGddDailyRepository.findSavedDatesByCropsIdAndDateRange(
                crop.getCropsId(), from, to
        );
        Set<LocalDate> savedDates = new HashSet<>(saved);
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (!savedDates.contains(date)) {
                upsertOneDay(crop, date);
            }
        }
    }

    @Transactional
    public void upsertOneDay(Crops crop, LocalDate targetDate) {
        if (crop == null || targetDate == null) {
            return;
        }
        if (crop.getPlantingDate() == null || targetDate.isBefore(crop.getPlantingDate())) {
            return;
        }

        BigDecimal baseTemp = resolveBaseTemp(crop);
        if (baseTemp == null) {
            return;
        }

        OffsetDateTime from = targetDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to = targetDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1);

        List<BigDecimal[]> minMaxRows = sensorReadingRepository.findDailyMinMaxByFarmIdAndSensorType(
                crop.getFarm().getId(),
                SensorType.SOIL_TEMPERATURE,
                from,
                to
        );

        BigDecimal dailyGdd = BigDecimal.ZERO;
        String source = SOURCE_NO_SENSOR_DATA;

        if (!minMaxRows.isEmpty()) {
            BigDecimal[] minMax = minMaxRows.get(0);
            BigDecimal minTemp = minMax[0];
            BigDecimal maxTemp = minMax[1];
            if (minTemp != null && maxTemp != null) {
                BigDecimal meanTemp = minTemp.add(maxTemp)
                        .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
                dailyGdd = meanTemp.subtract(baseTemp);
                if (dailyGdd.compareTo(BigDecimal.ZERO) < 0) {
                    dailyGdd = BigDecimal.ZERO;
                }
                dailyGdd = dailyGdd.setScale(2, RoundingMode.HALF_UP);
                source = SOURCE_SENSOR;
            }
        }

        cropGddDailyRepository.upsert(
                crop.getCropsId(),
                targetDate,
                dailyGdd,
                null,
                baseTemp,
                "SENSOR",
                String.valueOf(crop.getFarm().getId()),
                source,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    private BigDecimal resolveBaseTemp(Crops crop) {
        if (crop.getBaseTemp() != null) {
            return crop.getBaseTemp();
        }
        if (crop.getGrowthStandard() != null) {
            return crop.getGrowthStandard().getBaseTemp();
        }
        return null;
    }
}
