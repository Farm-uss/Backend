package com.example.practice.service.device;

import com.example.practice.common.error.BusinessException;
import com.example.practice.common.error.DeviceNotFoundException;
import com.example.practice.common.error.ErrorCode;
import com.example.practice.dto.Device.DeviceRegisterRequest;
import com.example.practice.dto.Device.DeviceResponse;
import com.example.practice.dto.Device.DeviceStatusUpdateRequest;
import com.example.practice.entity.device.Device;
import com.example.practice.repository.device.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceResponse register(DeviceRegisterRequest request) {
        if (deviceRepository.existsByDeviceUuid(request.getDeviceUuid())) {
            throw new BusinessException(ErrorCode.DEVICE_UUID_DUPLICATE,
                    "uuid: " + request.getDeviceUuid());
        }
        Device device = Device.create(request.getDeviceUuid(), request.getName(), request.getFarmId());
        return DeviceResponse.from(deviceRepository.save(device));
    }

    public DeviceResponse getById(Long deviceId) {
        return DeviceResponse.from(findById(deviceId));
    }

    public List<DeviceResponse> getByFarmId(Long farmId) {
        return deviceRepository.findAllByFarmId(farmId).stream()
                .map(DeviceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceResponse updateStatus(Long deviceId, DeviceStatusUpdateRequest request) {
        Device device = findById(deviceId);
        device.changeStatus(request.getStatus());
        return DeviceResponse.from(device);
    }

    /**
     * 데이터 수신 시마다 호출 - lastSeenAt 갱신
     */
    @Transactional
    public void refreshLastSeenAt(Long deviceId) {
        Device device = findById(deviceId);
        device.refreshLastSeenAt(OffsetDateTime.now());
    }

    @Transactional
    public void delete(Long deviceId) {
        deviceRepository.delete(findById(deviceId));
    }

    // ─── 패키지 내부 공유 메서드 ──────────────────────────────────────

    public Device findById(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));
    }
}
