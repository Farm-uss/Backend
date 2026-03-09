package com.example.practice.service.crops;

import com.example.practice.common.error.AppException;
import com.example.practice.dto.crops.GddSummaryResponse;
import com.example.practice.dto.crops.GddTimeSeriesResponse;
import com.example.practice.dto.crops.GrowthDiaryDetailResponse;
import com.example.practice.dto.crops.DiseaseLatestResponse;
import com.example.practice.dto.crops.GrowthMetricResponse;
import com.example.practice.entity.crops.CropGddDaily;
import com.example.practice.entity.crops.CropGrowthStandard;
import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.crops.GrowthMeasurement;
import com.example.practice.entity.crops.GrowthMetricSource;
import com.example.practice.entity.crops.GrowthMetricType;
import com.example.practice.repository.crops.CropGddDailyRepository;
import com.example.practice.repository.crops.CropsRepository;
import com.example.practice.repository.crops.GrowthMeasurementRepository;
import com.example.practice.repository.farm.FarmMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import com.example.practice.dto.crops.GrowthDiaryCardResponse;

@Service
@RequiredArgsConstructor
public class GddSummaryService {

    private static final BigDecimal MIN_AVG_DAILY_GDD = BigDecimal.valueOf(0.01);

    private final CropsRepository cropsRepository;
    private final CropGddDailyRepository cropGddDailyRepository;
    private final GrowthMeasurementRepository growthMeasurementRepository;
    private final FarmMemberRepository farmMemberRepository;
    private final SensorGddIngestionService sensorGddIngestionService;

    @Transactional(readOnly = true)
    public GddSummaryResponse getSummary(Long farmId, Long cropsId, Long userId) {
        Crops crop = getAccessibleCrop(farmId, cropsId, userId);

        if (crop.getPlantingDate() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "plantingDate is required");
        }

        BigDecimal targetGdd = resolveTargetGdd(crop);
        if (targetGdd == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "targetGdd is required");
        }

        LocalDate today = LocalDate.now();
        LocalDate plantingDate = crop.getPlantingDate();
        sensorGddIngestionService.ensureDateRangeSaved(crop, plantingDate, today);

        BigDecimal currentGdd = cropGddDailyRepository.sumGddByCropsIdAndDateRange(cropsId, plantingDate, today);
        LocalDate expectedHarvestDate = calculateExpectedHarvestDate(cropsId, plantingDate, today, currentGdd, targetGdd);
        boolean hasNoSensorDay = cropGddDailyRepository.existsByCropsIdAndDateRangeAndSource(
                cropsId,
                plantingDate,
                today,
                SensorGddIngestionService.SOURCE_NO_SENSOR_DATA
        );

        Integer currentDays = toInclusiveDays(plantingDate, today);
        Integer targetDays = expectedHarvestDate == null ? null : toInclusiveDays(plantingDate, expectedHarvestDate);

        return new GddSummaryResponse(
                farmId,
                cropsId,
                targetDays,
                currentDays,
                expectedHarvestDate,
                targetGdd,
                currentGdd,
                hasNoSensorDay ? "NO_SENSOR_DATA" : "OK",
                hasNoSensorDay ? "해당 기간에 센서 데이터가 없는 날짜가 있어 일부 값은 0으로 계산되었습니다." : null
        );
    }

    @Transactional(readOnly = true)
    public List<GddTimeSeriesResponse> getTimeSeries(
            Long farmId,
            Long cropsId,
            Long userId,
            LocalDate from,
            LocalDate to
    ) {
        if (from.isAfter(to)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "from must be less than or equal to to");
        }
        Crops crop = getAccessibleCrop(farmId, cropsId, userId);
        sensorGddIngestionService.ensureDateRangeSaved(crop, from, to);

        List<CropGddDaily> rows = cropGddDailyRepository
                .findAllByCrops_CropsIdAndTargetDateBetweenOrderByTargetDateAsc(cropsId, from, to);

        BigDecimal cumulative = BigDecimal.ZERO;
        List<GddTimeSeriesResponse> response = new ArrayList<>(rows.size());
        for (CropGddDaily row : rows) {
            BigDecimal daily = row.getGdd() == null ? BigDecimal.ZERO : row.getGdd();
            cumulative = cumulative.add(daily);
            response.add(new GddTimeSeriesResponse(row.getTargetDate(), daily, cumulative));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<GrowthMetricResponse> getGrowthMetrics(
            Long farmId,
            Long cropsId,
            Long userId,
            GrowthMetricType metric,
            LocalDate from,
            LocalDate to
    ) {
        getAccessibleCrop(farmId, cropsId, userId);

        if (from.isAfter(to)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "from must be less than or equal to to");
        }

        OffsetDateTime fromAt = from.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime toExclusive = to.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        List<GrowthMeasurement> rows = growthMeasurementRepository
                .findAllByCrops_CropsIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(cropsId, fromAt, toExclusive.minusNanos(1));

        return rows.stream()
                .map(row -> new GrowthMetricResponse(
                        row.getMeasuredAt().toLocalDate(),
                        resolveMetricValue(metric, row),
                        resolveSource(row),
                        null
                ))
                .filter(row -> row.value() != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GrowthDiaryCardResponse> getGrowthDiary(
            Long farmId,
            Long cropsId,
            Long userId,
            int year,
            int month
    ) {
        getAccessibleCrop(farmId, cropsId, userId);

        if (year < 1900 || year > 2100 || month < 1 || month > 12) {
            throw new AppException(HttpStatus.BAD_REQUEST, "year/month is out of range");
        }

        LocalDate monthStart;
        try {
            monthStart = LocalDate.of(year, month, 1);
        } catch (DateTimeException e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "invalid year/month");
        }
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        OffsetDateTime fromAt = monthStart.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime toAt = monthEnd.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset()).minusNanos(1);

        List<GrowthMeasurement> monthRows = growthMeasurementRepository
                .findAllByCrops_CropsIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(cropsId, fromAt, toAt);

        GrowthMeasurement previous = growthMeasurementRepository
                .findTopByCrops_CropsIdAndMeasuredAtLessThanOrderByMeasuredAtDesc(cropsId, fromAt)
                .orElse(null);

        // 날짜별 카드 1개만 필요하므로 같은 날짜는 가장 마지막 측정값을 사용한다.
        Map<LocalDate, GrowthMeasurement> latestByDate = new LinkedHashMap<>();
        for (GrowthMeasurement row : monthRows) {
            latestByDate.put(row.getMeasuredAt().toLocalDate(), row);
        }

        List<GrowthDiaryCardResponse> response = new ArrayList<>(latestByDate.size());
        for (GrowthMeasurement current : latestByDate.values()) {
            response.add(new GrowthDiaryCardResponse(
                    current.getMeasuredAt().toLocalDate(),
                    null,
                    diffInt(current.getLeafCount(), previous == null ? null : previous.getLeafCount()),
                    diffInt(current.getFruitCount(), previous == null ? null : previous.getFruitCount()),
                    diffDecimal(resolveSizeCm(current), previous == null ? null : resolveSizeCm(previous)),
                    null,
                    null
            ));
            previous = current;
        }

        return response;
    }

    @Transactional(readOnly = true)
    public GrowthDiaryDetailResponse getGrowthDiaryDetail(
            Long farmId,
            Long cropsId,
            Long userId,
            LocalDate date
    ) {
        Crops crop = getAccessibleCrop(farmId, cropsId, userId);

        OffsetDateTime dayStart = date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset()).minusNanos(1);

        GrowthMeasurement growthMeasurement = growthMeasurementRepository
                .findTopByCrops_CropsIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(cropsId, dayStart, dayEnd)
                .orElse(null);

        CropGddDaily gddDaily = cropGddDailyRepository
                .findByCrops_CropsIdAndTargetDate(cropsId, date)
                .orElse(null);

        GrowthDiaryDetailResponse.Growth growth = growthMeasurement == null
                ? null
                : new GrowthDiaryDetailResponse.Growth(
                growthMeasurement.getLeafCount(),
                growthMeasurement.getFruitCount(),
                resolveSizeCm(growthMeasurement)
        );

        GrowthDiaryDetailResponse.Gdd gdd = null;
        if (gddDaily != null) {
            BigDecimal cumulative;
            if (crop.getPlantingDate() != null && !crop.getPlantingDate().isAfter(date)) {
                cumulative = cropGddDailyRepository.sumGddByCropsIdAndDateRange(cropsId, crop.getPlantingDate(), date);
            } else {
                cumulative = gddDaily.getGdd();
            }
            gdd = new GrowthDiaryDetailResponse.Gdd(gddDaily.getGdd(), cumulative);
        }

        GrowthDiaryDetailResponse.Disease disease = toDisease(growthMeasurement);

        if (growth == null && gdd == null && disease == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "growth diary not found for date");
        }

        return new GrowthDiaryDetailResponse(
                date,
                null,
                null,
                growth,
                gdd,
                disease
        );
    }

    @Transactional(readOnly = true)
    public DiseaseLatestResponse getLatestDisease(Long farmId, Long cropsId, Long userId) {
        getAccessibleCrop(farmId, cropsId, userId);

        GrowthMeasurement latest = growthMeasurementRepository
                .findTopByCrops_CropsIdAndAiConfidenceIsNotNullOrderByMeasuredAtDesc(cropsId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "latest disease inference not found"));

        String verdict = safeText(latest.getAiVerdict());
        String label = safeText(latest.getAiLabel());

        String diseaseName = verdict != null ? verdict : label;
        if (diseaseName == null) {
            diseaseName = "unknown";
        }

        String status = isHealthyLike(diseaseName) ? "NORMAL" : "ABNORMAL";
        return new DiseaseLatestResponse(status, diseaseName, latest.getAiConfidence(), latest.getMeasuredAt());
    }

    private BigDecimal resolveMetricValue(GrowthMetricType metric, GrowthMeasurement row) {
        return switch (metric) {
            case LEAF_COUNT -> row.getLeafCount() == null ? null : BigDecimal.valueOf(row.getLeafCount());
            case FRUIT_COUNT -> row.getFruitCount() == null ? null : BigDecimal.valueOf(row.getFruitCount());
            case HEIGHT_CM -> row.getHeight();
            case SIZE_CM -> row.getLeafArea();
        };
    }

    private GrowthMetricSource resolveSource(GrowthMeasurement row) {
        if (row.getAiConfidence() != null || row.getAiRawJson() != null || row.getAiSummary() != null) {
            return GrowthMetricSource.AI;
        }
        return GrowthMetricSource.MANUAL;
    }

    private BigDecimal resolveSizeCm(GrowthMeasurement row) {
        return row.getLeafArea();
    }

    private GrowthDiaryDetailResponse.Disease toDisease(GrowthMeasurement row) {
        if (row == null || row.getAiConfidence() == null) {
            return null;
        }

        String verdict = safeText(row.getAiVerdict());
        String label = safeText(row.getAiLabel());
        String diseaseName = verdict != null ? verdict : label;
        if (diseaseName == null) {
            diseaseName = "unknown";
        }

        return new GrowthDiaryDetailResponse.Disease(diseaseName, row.getAiConfidence());
    }

    private String safeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isHealthyLike(String diseaseName) {
        if (diseaseName == null) {
            return true;
        }
        String normalized = diseaseName.trim().toLowerCase(Locale.ROOT);
        return "healthy".equals(normalized)
                || "normal".equals(normalized)
                || "00".equals(normalized)
                || "unknown".equals(normalized);
    }

    private Integer diffInt(Integer current, Integer previous) {
        if (current == null || previous == null) {
            return null;
        }
        return current - previous;
    }

    private BigDecimal diffDecimal(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null) {
            return null;
        }
        return current.subtract(previous);
    }

    private LocalDate calculateExpectedHarvestDate(
            Long cropsId,
            LocalDate plantingDate,
            LocalDate today,
            BigDecimal currentGdd,
            BigDecimal targetGdd
    ) {
        BigDecimal remaining = targetGdd.subtract(currentGdd);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return today;
        }

        LocalDate recentFrom = today.minusDays(6);
        if (recentFrom.isBefore(plantingDate)) {
            recentFrom = plantingDate;
        }

        List<CropGddDaily> recent = cropGddDailyRepository
                .findAllByCrops_CropsIdAndTargetDateBetweenOrderByTargetDateAsc(cropsId, recentFrom, today);

        BigDecimal recentSum = recent.stream()
                .map(CropGddDaily::getGdd)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long recentDays = ChronoUnit.DAYS.between(recentFrom, today) + 1;
        BigDecimal avgDaily = recentDays <= 0
                ? BigDecimal.ZERO
                : recentSum.divide(BigDecimal.valueOf(recentDays), 4, RoundingMode.HALF_UP);

        if (avgDaily.compareTo(MIN_AVG_DAILY_GDD) < 0) {
            return null;
        }

        long daysNeeded = remaining.divide(avgDaily, 0, RoundingMode.CEILING).longValue();
        return today.plusDays(daysNeeded);
    }

    private Integer toInclusiveDays(LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        return (int) Math.max(days, 0L);
    }

    private BigDecimal resolveTargetGdd(Crops crop) {
        if (crop.getTargetGdd() != null) {
            return crop.getTargetGdd();
        }

        CropGrowthStandard growthStandard = crop.getGrowthStandard();
        if (growthStandard == null) {
            return null;
        }

        if (growthStandard.getTargetGdd() != null) {
            return growthStandard.getTargetGdd();
        }

        if (growthStandard.getTargetGddHarvest() != null) {
            return BigDecimal.valueOf(growthStandard.getTargetGddHarvest());
        }

        return null;
    }

    private Crops getAccessibleCrop(Long farmId, Long cropsId, Long userId) {
        if (!farmMemberRepository.existsByFarmIdAndUserId(farmId, userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, "farm access denied");
        }

        return cropsRepository.findByFarmIdAndCropsIdWithGrowthStandard(farmId, cropsId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "crops not found in farm"));
    }
}
