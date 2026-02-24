package com.example.practice.service.crops;

import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.farm.Farm;
import com.example.practice.entity.farm.StationType;
import com.example.practice.repository.crops.CropGddDailyRepository;
import com.example.practice.repository.crops.CropsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GddIngestionService {

    private final CropsRepository cropsRepository;
    private final CropGddDailyRepository dailyRepository;
    private final GddApiClient apiClient;
    private final String serviceKey;

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public GddIngestionService(CropsRepository cropsRepository,
                               CropGddDailyRepository dailyRepository,
                               GddApiClient apiClient,
                               @Value("${gdd.api.service-key}") String serviceKey) {
        this.cropsRepository = cropsRepository;
        this.dailyRepository = dailyRepository;
        this.apiClient = apiClient;
        this.serviceKey = serviceKey;
    }


    @Transactional
    public int refresh(Long cropId, int days) {
        Crops crop = cropsRepository.findByIdWithFarm(cropId)
                .orElseThrow(() -> new IllegalArgumentException("crops not found: " + cropId));

        Farm farm = crop.getFarm();
        validateFarmStation(farm);

        LocalDate end = LocalDate.now();
        LocalDate begin = end.minusDays(Math.max(days, 1) - 1);

        String beginDate = begin.format(YMD);
        String endDate = end.format(YMD);


        String cropCode = crop.getGrowthStandard().getCropCode(); // 예: "01"~"12" or "99"
        BigDecimal baseTemp = crop.getBaseTemp();                 // Tb

        String growthTempIf99 = "99".equals(cropCode) ? baseTemp.toPlainString() : null;

        String xml = (farm.getStationType() == StationType.SPOT)
                ? apiClient.fetchSpotXml(serviceKey, farm.getObsrSpotCode(), beginDate, endDate, cropCode, growthTempIf99).block()
                : apiClient.fetchZoneXml(serviceKey, farm.getZoneCode(), beginDate, endDate, cropCode, growthTempIf99).block();

        List<GddXmlParser.Row> rows = GddXmlParser.parse(xml);

        String stationCode = (farm.getStationType() == StationType.SPOT) ? farm.getObsrSpotCode() : farm.getZoneCode();
        OffsetDateTime fetchedAt = OffsetDateTime.now();

        for (var r : rows) {
            LocalDate d = LocalDate.parse(r.dateYmd(), YMD);
            dailyRepository.upsert(
                    crop.getCropsId(),
                    d,
                    r.gdd(),
                    r.gdd5y(),
                    baseTemp,
                    farm.getStationType().name(),
                    stationCode,
                    "KMA_OPENAPI",
                    fetchedAt
            );
        }

        return rows.size();
    }


    @Transactional
    public void ensureDailyGddSaved(Crops crop, LocalDate plantingDate, LocalDate today) {
        if (plantingDate == null) throw new IllegalArgumentException("plantingDate is null");
        if (today == null) throw new IllegalArgumentException("today is null");
        if (today.isBefore(plantingDate)) return;

        // 1) plantingDate 하루라도 없으면 -> 전체 구간 refresh
        boolean hasFirstDay = dailyRepository
                .findByCrops_CropsIdAndTargetDate(crop.getCropsId(), plantingDate)
                .isPresent();

        // 2) 오늘 데이터 없으면 -> 전체 구간 refresh
        boolean hasToday = dailyRepository
                .findByCrops_CropsIdAndTargetDate(crop.getCropsId(), today)
                .isPresent();

        if (!hasFirstDay || !hasToday) {
            int days = (int) (today.toEpochDay() - plantingDate.toEpochDay() + 1);
            refresh(crop.getCropsId(), days);
            return;
        }

        // 3) 중간 누락 체크(간단 버전): 범위 조회해서 개수 비교
        List<?> list = dailyRepository.findAllByCrops_CropsIdAndTargetDateBetweenOrderByTargetDateAsc(
                crop.getCropsId(), plantingDate, today
        );

        int expected = (int) (today.toEpochDay() - plantingDate.toEpochDay() + 1);
        if (list.size() != expected) {
            int days = expected;
            refresh(crop.getCropsId(), days);
        }
    }

    private void validateFarmStation(Farm farm) {
        if (farm.getStationType() == StationType.SPOT) {
            if (farm.getObsrSpotCode() == null || farm.getObsrSpotCode().isBlank())
                throw new IllegalStateException("farm obsrSpotCode required for SPOT");
        } else {
            if (farm.getZoneCode() == null || farm.getZoneCode().isBlank())
                throw new IllegalStateException("farm zoneCode required for ZONE");
        }
    }
}
