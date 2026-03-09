package com.example.practice.service.dashboard;

import com.example.practice.dto.dashboard.DashboardMetricDto;
import com.example.practice.dto.dashboard.DashboardResponseDto;
import com.example.practice.dto.dashboard.MetricStatus;
import com.example.practice.dto.dashboard.SensorStatus;
import com.example.practice.entity.environment.CropEnvironmentStandard;
import com.example.practice.entity.crops.CropGrowthStandard;
import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.device.EnvData;
import com.example.practice.repository.crops.CropEnvironmentStandardRepository;
import com.example.practice.repository.crops.CropsRepository;
import com.example.practice.repository.device.EnvDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CropsRepository cropsRepository;
    private final CropEnvironmentStandardRepository cropEnvironmentStandardRepository;
    private final EnvDataRepository envDataRepository;

    public DashboardResponseDto getDashboard(Long farmId) {
        Crops currentCrop = cropsRepository.findCurrentCropCandidatesByFarmId(farmId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("현재 농장 작물 정보가 없습니다."));

        CropGrowthStandard growthStandard = currentCrop.getGrowthStandard();
        if (growthStandard == null || growthStandard.getCropCode() == null) {
            throw new IllegalArgumentException("작물의 growth standard 또는 crop code가 연결되어 있지 않습니다.");
        }

        CropEnvironmentStandard envStandard = cropEnvironmentStandardRepository.findByCropCode(growthStandard.getCropCode())
                .orElseThrow(() -> new IllegalArgumentException("작물 환경 기준 정보가 없습니다."));

        EnvData latestData = envDataRepository.findTopByDevice_FarmIdOrderByMeasuredAtDesc(farmId)
                .orElseThrow(() -> new IllegalArgumentException("환경 데이터가 없습니다."));

        SensorStatus sensorStatus = getSensorStatus(latestData);
        String sensorMessage = sensorStatus == SensorStatus.NORMAL
                ? "모든 센서가 정상적으로 작동 중입니다."
                : "센서 작동에 오류가 있습니다.";

        return new DashboardResponseDto(
                sensorStatus,
                sensorMessage,
                buildMetric("온도", toDouble(latestData.getTemp()), "°C",
                        envStandard.getMinTemp(), envStandard.getMaxTemp()),

                buildMetric("pH", toDouble(latestData.getPh()), "",
                        envStandard.getMinPh(), envStandard.getMaxPh()),

                buildMetric("토양 수분", toDouble(latestData.getSoilMoisture()), "%",
                        envStandard.getMinSoilMoisture(), envStandard.getMaxSoilMoisture()),

                buildMetric("CO2", toDouble(latestData.getCo2()), "ppm",
                        envStandard.getMinCo2(), envStandard.getMaxCo2()),

                buildMetric("EC", toDouble(latestData.getEc()), "mS/cm",
                        envStandard.getMinEc(), envStandard.getMaxEc()),

                buildMetric("조도", toDouble(latestData.getIlluminance()), "lux",
                        envStandard.getMinLight(), envStandard.getMaxLight())
        );
    }

    private DashboardMetricDto buildMetric(String label, Double value, String unit, Double min, Double max) {
        return new DashboardMetricDto(
                label,
                value,
                unit,
                min,
                max,
                calculateStatus(value, min, max)
        );
    }

    private MetricStatus calculateStatus(Double value, Double min, Double max) {
        if (value == null) {
            return MetricStatus.NO_DATA;
        }
        if (min != null && value < min) {
            return MetricStatus.LOW;
        }
        if (max != null && value > max) {
            return MetricStatus.HIGH;
        }
        return MetricStatus.NORMAL;
    }

    private SensorStatus getSensorStatus(EnvData data) {
        if (data.getMeasuredAt() == null) {
            return SensorStatus.ERROR;
        }

        if (data.getMeasuredAt().isBefore(OffsetDateTime.now().minusMinutes(5))) {
            return SensorStatus.ERROR;
        }

        if (data.getTemp() == null ||
                data.getPh() == null ||
                data.getSoilMoisture() == null ||
                data.getCo2() == null ||
                data.getEc() == null ||
                data.getIlluminance() == null) {
            return SensorStatus.ERROR;
        }

        return SensorStatus.NORMAL;
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}