package com.example.practice.service.dashboard;

import com.example.practice.dto.dashboard.DashboardMetricDto;
import com.example.practice.dto.dashboard.DashboardResponseDto;
import com.example.practice.entity.environment.CropEnvironmentStandard;
import com.example.practice.entity.crops.CropGrowthStandard;
import com.example.practice.entity.crops.Crops;
import com.example.practice.repository.crops.CropEnvironmentStandardRepository;
import com.example.practice.repository.crops.CropsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CropsRepository cropsRepository;
    private final CropEnvironmentStandardRepository cropEnvironmentStandardRepository;

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

        return new DashboardResponseDto(
                currentCrop.getName(),
                growthStandard.getCropCode(),
                buildMetric("온도", "°C", envStandard.getMinTemp(), envStandard.getMaxTemp()),
                buildMetric("pH", "", envStandard.getMinPh(), envStandard.getMaxPh()),
                buildMetric("토양 수분", "%", envStandard.getMinSoilMoisture(), envStandard.getMaxSoilMoisture()),
                buildMetric("CO2", "ppm", envStandard.getMinCo2(), envStandard.getMaxCo2()),
                buildMetric("EC", "mS/cm", envStandard.getMinEc(), envStandard.getMaxEc()),
                buildMetric("조도", "lux", envStandard.getMinLight(), envStandard.getMaxLight())
        );
    }

    private DashboardMetricDto buildMetric(String label, String unit, Double min, Double max) {
        return new DashboardMetricDto(
                label,
                unit,
                min,
                max
        );
    }
}