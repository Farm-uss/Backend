package com.example.practice.service.notification;

import com.example.practice.entity.environment.CropEnvironmentStandard;
import com.example.practice.entity.crops.Crops;
import com.example.practice.entity.device.EnvData;
import com.example.practice.entity.farm.FarmMember;
import com.example.practice.entity.notification.Notification;
import com.example.practice.entity.notification.NotificationType;
import com.example.practice.repository.crops.CropEnvironmentStandardRepository;
import com.example.practice.repository.crops.CropsRepository;
import com.example.practice.repository.farm.FarmMemberRepository;
import com.example.practice.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertCheckService {

    private final CropsRepository cropsRepository;
    private final CropEnvironmentStandardRepository standardRepository;
    private final FarmMemberRepository farmMemberRepository;
    private final NotificationRepository notificationRepository;

    /**
     * EnvData 저장 직후 호출.
     *
     * 체크 항목 (실제 수신 센서 기준):
     *   - temp          → min_temp / max_temp
     *   - soilMoisture  → min_soil_moisture / max_soil_moisture
     *   - illuminance   → min_light / max_light
     *   - ec            → min_ec / max_ec
     *   - co2           → min_co2 / max_co2
     *   - ph            → min_ph / max_ph
     */
    @Transactional
    public void checkAndNotify(EnvData envData, Long farmId) {
        // 1. 농장의 작물 조회
        List<Crops> cropsList = cropsRepository.findAllByFarm_Id(farmId);
        if (cropsList.isEmpty()) {
            log.debug("[AlertCheck] farmId={} 작물 없음 - 체크 건너뜀", farmId);
            return;
        }

        String cropCode = cropsList.get(0).getCropCode();
        String cropName = cropsList.get(0).getName();

        // 2. 환경 기준 조회
        CropEnvironmentStandard std = standardRepository.findByCropCode(cropCode)
                .orElse(null);
        if (std == null) {
            log.debug("[AlertCheck] cropCode={} 환경 기준 없음 - 체크 건너뜀", cropCode);
            return;
        }

        // 3. 알림 받을 멤버 목록 (OWNER + MEMBER 전원)
        List<Long> memberIds = farmMemberRepository.findAllByFarmId(farmId)
                .stream()
                .map(FarmMember::getUserId)
                .toList();

        if (memberIds.isEmpty()) return;

        // 4. 항목별 범위 체크 → 알림 생성
        List<Notification> notifications = new ArrayList<>();

        checkValue(envData.getTemp(),
                std.getMinTemp(), std.getMaxTemp(),
                cropName, "온도", "°C",
                NotificationType.TEMP_LOW, NotificationType.TEMP_HIGH,
                farmId, memberIds, notifications);

        checkValue(envData.getSoilMoisture(),
                std.getMinSoilMoisture(), std.getMaxSoilMoisture(),
                cropName, "토양수분", "%",
                NotificationType.SOIL_MOISTURE_LOW, NotificationType.SOIL_MOISTURE_HIGH,
                farmId, memberIds, notifications);

        checkValue(envData.getIlluminance(),
                std.getMinLight(), std.getMaxLight(),
                cropName, "조도", "lux",
                NotificationType.LIGHT_LOW, NotificationType.LIGHT_HIGH,
                farmId, memberIds, notifications);

        checkValue(envData.getEc(),
                std.getMinEc(), std.getMaxEc(),
                cropName, "EC", "dS/m",
                NotificationType.EC_LOW, NotificationType.EC_HIGH,
                farmId, memberIds, notifications);

        checkValue(envData.getCo2(),
                std.getMinCo2(), std.getMaxCo2(),
                cropName, "CO2", "ppm",
                NotificationType.CO2_LOW, NotificationType.CO2_HIGH,
                farmId, memberIds, notifications);

        checkValue(envData.getPh(),
                std.getMinPh(), std.getMaxPh(),
                cropName, "pH", "",
                NotificationType.PH_LOW, NotificationType.PH_HIGH,
                farmId, memberIds, notifications);

        // 5. 알림 일괄 저장
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
            log.info("[AlertCheck] farmId={}, cropCode={} → {}건 알림 생성",
                    farmId, cropCode, notifications.size());
        }
    }

    // ─── private helper ──────────────────────────────────────────────

    private void checkValue(BigDecimal value,
                            Double min, Double max,
                            String cropName, String sensorName, String unit,
                            NotificationType lowType, NotificationType highType,
                            Long farmId, List<Long> userIds,
                            List<Notification> result) {
        if (value == null) return;

        double val = value.doubleValue();

        if (min != null && val < min) {
            String msg = String.format("[%s] %s가 %.1f%s로 최솟값(%.1f%s) 미만입니다.",
                    cropName, sensorName, val, unit, min, unit);
            userIds.forEach(uid -> result.add(Notification.create(uid, farmId, msg, lowType)));

        } else if (max != null && val > max) {
            String msg = String.format("[%s] %s가 %.1f%s로 최댓값(%.1f%s)을 초과했습니다.",
                    cropName, sensorName, val, unit, max, unit);
            userIds.forEach(uid -> result.add(Notification.create(uid, farmId, msg, highType)));
        }
    }
}
