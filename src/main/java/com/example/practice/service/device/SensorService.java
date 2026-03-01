package com.example.practice.service.device;

import com.example.practice.common.error.BusinessException;
import com.example.practice.common.error.ErrorCode;
import com.example.practice.common.error.SensorNotFoundException;
import com.example.practice.dto.Device.SensorRegisterRequest;
import com.example.practice.dto.Device.SensorResponse;
import com.example.practice.entity.device.Device;
import com.example.practice.entity.device.Sensor;
import com.example.practice.entity.device.SensorType;
import com.example.practice.repository.device.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorService {

    private final SensorRepository sensorRepository;
    private final DeviceService deviceService;

    @Transactional
    public SensorResponse register(SensorRegisterRequest request) {
        if (sensorRepository.existsByDevice_DeviceIdAndSensorType(
                request.getDeviceId(), request.getSensorType())) {
            throw new BusinessException(ErrorCode.SENSOR_TYPE_DUPLICATE,
                    "deviceId: " + request.getDeviceId() + ", type: " + request.getSensorType());
        }
        Device device = deviceService.findById(request.getDeviceId());
        Sensor sensor = Sensor.create(
                request.getSensorType(),
                device,
                        request.getUnit(),
                        request.getInstallLocation()
                );
        return SensorResponse.from(sensorRepository.save(sensor));
    }

    public List<SensorResponse> getByDeviceId(Long deviceId) {
        return sensorRepository.findAllByDevice_DeviceId(deviceId).stream()
                .map(SensorResponse::from)
                .collect(Collectors.toList());
    }

    public SensorResponse getById(Long sensorId) {
        return SensorResponse.from(findById(sensorId));
    }

    @Transactional
    public void delete(Long sensorId) {
        sensorRepository.delete(findById(sensorId));
    }

    // ─── 패키지 내부 공유 메서드 ──────────────────────────────────────

    public Sensor findById(Long sensorId) {
        return sensorRepository.findById(sensorId)
                .orElseThrow(() -> new SensorNotFoundException(sensorId));
    }

    public Optional<Sensor> findByDeviceAndType(Long deviceId, SensorType sensorType) {
        return sensorRepository.findByDevice_DeviceIdAndSensorType(deviceId, sensorType);
    }
}
