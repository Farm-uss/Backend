package com.example.practice.service.device;

import com.example.practice.common.error.EnvDataNotFoundException;
import com.example.practice.dto.Device.EnvDataResponse;
import com.example.practice.dto.Device.EnvDataSaveRequest;
import com.example.practice.entity.device.Device;
import com.example.practice.entity.device.EnvData;
import com.example.practice.entity.device.Sensor;
import com.example.practice.entity.device.SensorType;
import com.example.practice.repository.device.EnvDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.practice.service.notification.AlertCheckService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnvDataService {

    private final EnvDataRepository envDataRepository;
    private final DeviceService deviceService;
    private final SensorService sensorService;
    private final SensorReadingService sensorReadingService;
    private final AlertCheckService alertCheckService;

    @Transactional
    public EnvDataResponse save(EnvDataSaveRequest request) {
        Device device = deviceService.findById(request.getDeviceId());
        OffsetDateTime measuredAt = resolvedMeasuredAt(request.getMeasuredAt());

        // 1. 묶음 저장
        // humidity 제거 → null 고정, ph 추가
        EnvData envData = EnvData.create(
                request.getTemp(),
                null,
                request.getPh(),
                request.getSoilMoisture(),
                request.getIlluminance(),
                request.getEc(),
                request.getCo2(),
                device
        );
        envDataRepository.save(envData);

        // 2. 센서별 개별 reading 저장
        buildReadingMap(request).forEach((sensorType, value) ->
                trySaveReading(device.getDeviceId(), sensorType, value, measuredAt)
        );

        // 3. lastSeenAt 갱신
        device.refreshLastSeenAt(measuredAt);



        return EnvDataResponse.from(envData);
    }

    public EnvDataResponse getById(Long envDataId) {
        return EnvDataResponse.from(findById(envDataId));
    }

    public Page<EnvDataResponse> getPageByDeviceId(Long deviceId, Pageable pageable) {
        return envDataRepository
                .findAllByDevice_DeviceIdOrderByCreatedAtDesc(deviceId, pageable)
                .map(EnvDataResponse::from);
    }

    public Long getLatestEnvDataIdByDeviceId(Long deviceId) {
        return envDataRepository.findTopByDevice_DeviceIdOrderByCreatedAtDesc(deviceId)
                .map(EnvData::getEnvDataId)
                .orElseThrow(() -> new EnvDataNotFoundException(deviceId));
    }

    // ─── private helpers ─────────────────────────────────────────────

    private Map<SensorType, BigDecimal> buildReadingMap(EnvDataSaveRequest req) {
        Map<SensorType, BigDecimal> map = new EnumMap<>(SensorType.class);
        // 실제 수신하는 센서 항목만 매핑
        if (req.getTemp() != null)         map.put(SensorType.SOIL_TEMPERATURE, req.getTemp());
        if (req.getSoilMoisture() != null) map.put(SensorType.SOIL_MOISTURE,    req.getSoilMoisture());
        if (req.getIlluminance() != null)  map.put(SensorType.ILLUMINANCE,      req.getIlluminance());
        if (req.getEc() != null)           map.put(SensorType.EC,               req.getEc());
        if (req.getCo2() != null)          map.put(SensorType.CO2,              req.getCo2());
        if (req.getPh() != null)        map.put(SensorType.PH,               req.getPh());
        return map;
    }

    private void trySaveReading(Long deviceId, SensorType sensorType,
                                BigDecimal value, OffsetDateTime measuredAt) {
        Optional<Sensor> sensorOpt = sensorService.findByDeviceAndType(deviceId, sensorType);
        if (sensorOpt.isPresent()) {
            sensorReadingService.saveInternal(sensorOpt.get(), value, measuredAt);
        } else {
            log.debug("센서 미등록 - 건너뜀 [deviceId={}, sensorType={}]", deviceId, sensorType);
        }
    }

    private OffsetDateTime resolvedMeasuredAt(OffsetDateTime measuredAt) {
        return (measuredAt != null) ? measuredAt : OffsetDateTime.now();
    }

    private EnvData findById(Long envDataId) {
        return envDataRepository.findById(envDataId)
                .orElseThrow(() -> new EnvDataNotFoundException(envDataId));
    }
}
