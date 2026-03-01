package com.example.practice.service.device;

import com.example.practice.dto.Device.SensorReadingResponse;
import com.example.practice.dto.Device.SensorReadingSaveRequest;
import com.example.practice.entity.device.Sensor;
import com.example.practice.entity.device.SensorReading;
import com.example.practice.repository.device.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;
    private final SensorService sensorService;

    @Transactional
    public SensorReadingResponse save(SensorReadingSaveRequest request) {
        Sensor sensor = sensorService.findById(request.getSensorId());
        SensorReading reading = SensorReading.create(
                request.getValue(),
                request.getMeasuredAt(),
                sensor
        );
        return SensorReadingResponse.from(sensorReadingRepository.save(reading));
    }

    /**
     * 서비스 내부 전용: 센서 객체와 시각을 직접 받아 저장
     * (EnvDataService에서 bulk 저장 시 사용)
     */
    @Transactional
    public SensorReading saveInternal(Sensor sensor, java.math.BigDecimal value,
                                      OffsetDateTime measuredAt) {
        return sensorReadingRepository.save(
                SensorReading.create(value, measuredAt, sensor)
        );
    }

    public Page<SensorReadingResponse> getPageBySensorId(Long sensorId, Pageable pageable) {
        return sensorReadingRepository
                .findAllBySensor_SensorIdOrderByMeasuredAtDesc(sensorId, pageable)
                .map(SensorReadingResponse::from);
    }

    public List<SensorReadingResponse> getByPeriod(Long sensorId,
                                                   OffsetDateTime from,
                                                   OffsetDateTime to) {
        return sensorReadingRepository
                .findAllBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(sensorId, from, to)
                .stream()
                .map(SensorReadingResponse::from)
                .collect(Collectors.toList());
    }

    public List<SensorReadingResponse> getByDeviceIdAndPeriod(Long deviceId,
                                                              OffsetDateTime from,
                                                              OffsetDateTime to) {
        return sensorReadingRepository.findAllByDeviceIdAndPeriod(deviceId, from, to)
                .stream()
                .map(SensorReadingResponse::from)
                .collect(Collectors.toList());
    }
}
