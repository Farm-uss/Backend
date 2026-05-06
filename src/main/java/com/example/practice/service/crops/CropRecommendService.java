package com.example.practice.service.crops;

import com.example.practice.dto.Device.SensorCheckDetail;
import com.example.practice.dto.Device.SensorValueSnapshot;
import com.example.practice.dto.crops.CropRecommendResponse;
import com.example.practice.dto.crops.CropRecommendation;
import com.example.practice.entity.device.EnvData;
import com.example.practice.entity.device.SensorType;
import com.example.practice.entity.environment.CropEnvironmentStandard;
import com.example.practice.repository.crops.CropEnvironmentStandardRepository;
import com.example.practice.repository.device.EnvDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CropRecommendService {

    private static final int MIN_MATCH_COUNT = 3;

    private final EnvDataRepository envDataRepository;
    private final CropEnvironmentStandardRepository cropEnvironmentStandardRepository;

    public CropRecommendResponse recommend(Long deviceId) {

        // ── 1. env_data에서 가장 최근 1건 조회 ───────────────────────
        EnvData latest = envDataRepository
                .findTopByDevice_DeviceIdOrderByCreatedAtDesc(deviceId)
                .orElseThrow(() -> new IllegalStateException(
                        "deviceId=" + deviceId + "에 환경 데이터가 없습니다."));

        // ── 2. EnvData → SensorType Map 변환 ─────────────────────────
        Map<SensorType, BigDecimal> latestValues = toSensorMap(latest);

        if (latestValues.isEmpty()) {
            throw new IllegalStateException("deviceId=" + deviceId + "에 측정 데이터가 없습니다.");
        }

        // ── 3. 현재 센서값 스냅샷 생성 (응답용) ──────────────────────
        SensorValueSnapshot snapshot = SensorValueSnapshot.from(latestValues);

        // ── 4. 모든 작물 환경 기준과 비교 ────────────────────────────
        List<CropEnvironmentStandard> allStandards =
                cropEnvironmentStandardRepository.findAll();

        List<CropRecommendation> recommendations = new ArrayList<>();
        for (CropEnvironmentStandard standard : allStandards) {
            evaluate(latestValues, standard).ifPresent(recommendations::add);
        }

        // ── 5. 매칭 수 내림차순 정렬 ─────────────────────────────────
        recommendations.sort(Comparator
                .comparingInt(CropRecommendation::getMatchedCount).reversed()
                .thenComparingDouble(CropRecommendation::getMatchRate).reversed());

        String message = recommendations.isEmpty()
                ? "현재 센서 환경에 적합한 작물이 없습니다."
                : recommendations.size() + "개의 작물이 현재 환경에 적합합니다.";

        return CropRecommendResponse.builder()
                .deviceId(deviceId)
                .sensorSnapshot(snapshot)
                .minMatchRequired(MIN_MATCH_COUNT)
                .recommendations(recommendations)
                .message(message)
                .build();
    }

    // ──────────────────────────────────────────────────────────────────
    // EnvData → SensorType별 값 Map 변환
    // ──────────────────────────────────────────────────────────────────
    private Map<SensorType, BigDecimal> toSensorMap(EnvData envData) {
        Map<SensorType, BigDecimal> map = new EnumMap<>(SensorType.class);

        if (envData.getTemp() != null)
            map.put(SensorType.SOIL_TEMPERATURE, envData.getTemp());
        if (envData.getSoilMoisture() != null)
            map.put(SensorType.SOIL_MOISTURE, envData.getSoilMoisture());
        if (envData.getEc() != null)
            map.put(SensorType.EC, envData.getEc());
        if (envData.getPh() != null)
            map.put(SensorType.PH, envData.getPh());
        if (envData.getCo2() != null)
            map.put(SensorType.CO2, envData.getCo2());
        if (envData.getIlluminance() != null)
            map.put(SensorType.ILLUMINANCE, envData.getIlluminance());

        return map;
    }

    // ── 이하 evaluate, addDetail 메서드는 기존 코드 그대로 ────────────

    private Optional<CropRecommendation> evaluate(Map<SensorType, BigDecimal> values,
                                                  CropEnvironmentStandard std) {
        List<SensorCheckDetail> details = new ArrayList<>();

        addDetail(details, SensorType.SOIL_TEMPERATURE, "토양 온도",  values, std.getMinTemp(),         std.getMaxTemp());
        addDetail(details, SensorType.PH,               "pH",        values, std.getMinPh(),           std.getMaxPh());
        addDetail(details, SensorType.SOIL_MOISTURE,    "토양 수분",  values, std.getMinSoilMoisture(), std.getMaxSoilMoisture());
        addDetail(details, SensorType.CO2,              "CO₂",       values, std.getMinCo2(),          std.getMaxCo2());
        addDetail(details, SensorType.EC,               "EC",        values, std.getMinEc(),           std.getMaxEc());
        addDetail(details, SensorType.ILLUMINANCE,      "조도",       values, std.getMinLight(),        std.getMaxLight());

        int totalChecked = details.size();
        int matchedCount = (int) details.stream().filter(SensorCheckDetail::isInRange).count();

        if (totalChecked == 0 || matchedCount < MIN_MATCH_COUNT) {
            return Optional.empty();
        }

        double matchRate = Math.round((double) matchedCount / totalChecked * 1000.0) / 10.0;

        return Optional.of(CropRecommendation.builder()
                .cropCode(std.getCropCode())
                .matchedCount(matchedCount)
                .totalChecked(totalChecked)
                .matchRate(matchRate)
                .details(details)
                .build());
    }

    private void addDetail(List<SensorCheckDetail> details,
                           SensorType type, String label,
                           Map<SensorType, BigDecimal> values,
                           Double min, Double max) {
        BigDecimal rawValue = values.get(type);
        if (rawValue == null) return;
        if (min == null && max == null) return;

        double value = rawValue.doubleValue();
        boolean inRange = (min == null || value >= min)
                && (max == null || value <= max);

        details.add(SensorCheckDetail.builder()
                .sensorType(type.name())
                .label(label)
                .unit(type.getDefaultUnit())
                .value(value)
                .min(min)
                .max(max)
                .inRange(inRange)
                .build());
    }
}