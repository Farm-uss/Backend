package com.example.practice.service.crops;

import com.example.practice.dto.Device.SensorCheckDetail;
import com.example.practice.dto.Device.SensorValueSnapshot;
import com.example.practice.dto.crops.CropRecommendResponse;
import com.example.practice.dto.crops.CropRecommendation;
import com.example.practice.entity.device.Sensor;
import com.example.practice.entity.device.SensorReading;
import com.example.practice.entity.device.SensorType;
import com.example.practice.entity.environment.CropEnvironmentStandard;
import com.example.practice.repository.crops.CropEnvironmentStandardRepository;
import com.example.practice.repository.device.SensorReadingRepository;
import com.example.practice.repository.device.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CropRecommendService {

    private static final int MIN_MATCH_COUNT = 3;

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final CropEnvironmentStandardRepository cropEnvironmentStandardRepository;

    /**
     * deviceId 산하의 모든 센서 최신값을 수집한 뒤,
     * crop_environment_standard 전체 로우와 비교하여 작물을 추천한다.
     *
     * - SensorType 별로 최신 SensorReading 1건씩 조회
     * - 각 SensorType 을 CropEnvironmentStandard 컬럼에 매핑
     * - MIN_MATCH_COUNT(3)개 이상 범위 내 → 추천 작물로 반환
     */
    public CropRecommendResponse recommend(Long deviceId) {

        // ── 1. 디바이스의 모든 센서 조회 ─────────────────────────────
        List<Sensor> sensors = sensorRepository.findAllByDevice_DeviceId(deviceId);
        if (sensors.isEmpty()) {
            throw new IllegalArgumentException("deviceId=" + deviceId + "에 등록된 센서가 없습니다.");
        }

        // ── 2. SensorType → 최신 측정값 Map 구성 ─────────────────────
        //    (device-sensor unique 제약으로 타입당 1개만 존재)
        Map<SensorType, BigDecimal> latestValues = collectLatestValues(sensors);

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
    // 센서 목록 → SensorType별 최신 측정값 Map
    // ──────────────────────────────────────────────────────────────────
    private Map<SensorType, BigDecimal> collectLatestValues(List<Sensor> sensors) {
        Map<SensorType, BigDecimal> map = new EnumMap<>(SensorType.class);

        for (Sensor sensor : sensors) {
            sensorReadingRepository
                    .findTopBySensor_SensorIdOrderByMeasuredAtDesc(sensor.getSensorId())
                    .map(SensorReading::getValue)
                    .ifPresent(value -> map.put(sensor.getSensorType(), value));
        }
        return map;
    }

    // ──────────────────────────────────────────────────────────────────
    // 작물 1건 평가
    // ──────────────────────────────────────────────────────────────────
    private Optional<CropRecommendation> evaluate(Map<SensorType, BigDecimal> values,
                                                  CropEnvironmentStandard std) {
        List<SensorCheckDetail> details = new ArrayList<>();

        // SensorType ↔ CropEnvironmentStandard 컬럼 매핑
        addDetail(details, SensorType.SOIL_TEMPERATURE, "토양 온도",  values, std.getMinTemp(),         std.getMaxTemp());
        addDetail(details, SensorType.PH,               "pH",        values, std.getMinPh(),           std.getMaxPh());
        addDetail(details, SensorType.SOIL_MOISTURE,    "토양 수분",  values, std.getMinSoilMoisture(), std.getMaxSoilMoisture());
        addDetail(details, SensorType.SOIL_HUMIDITY,    "토양 습도",  values, std.getMinSoilMoisture(), std.getMaxSoilMoisture());
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

    /**
     * values 맵에서 해당 SensorType 의 측정값을 꺼내 범위 비교 후 details 에 추가.
     * - 측정값이 없으면 생략
     * - min/max 가 모두 null 이면 기준 미등록으로 생략
     * - min 만 null → 하한 없음, max 만 null → 상한 없음
     */
    private void addDetail(List<SensorCheckDetail> details,
                           SensorType type,
                           String label,
                           Map<SensorType, BigDecimal> values,
                           Double min,
                           Double max) {
        BigDecimal rawValue = values.get(type);
        if (rawValue == null) return;          // 해당 타입 센서 미측정 → 생략
        if (min == null && max == null) return; // 기준 컬럼 미등록 → 생략

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
