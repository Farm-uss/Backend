package com.example.practice.service.schedule;

import com.example.practice.entity.device.SensorType;
import com.example.practice.repository.device.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SensorValueQueryService {

    private final SensorReadingRepository sensorReadingRepository;

    public BigDecimal getLatestFarmSensorValue(Long farmId, SensorType sensorType) {
        return sensorReadingRepository.findLatestValueByFarmIdAndSensorType(
                farmId,
                sensorType.name()
        ).orElse(null);
    }
}
