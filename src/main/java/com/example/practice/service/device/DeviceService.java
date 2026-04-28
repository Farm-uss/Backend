package com.example.practice.service.device;

import com.example.practice.common.error.BusinessException;
import com.example.practice.common.error.DeviceNotFoundException;
import com.example.practice.common.error.ErrorCode;
import com.example.practice.dto.Device.DeviceRegisterRequest;
import com.example.practice.dto.Device.DeviceResponse;
import com.example.practice.dto.Device.DeviceStatusUpdateRequest;
import com.example.practice.entity.device.Camera;
import com.example.practice.entity.device.Device;
import com.example.practice.repository.device.CameraRepository;
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
    private final CameraRepository cameraRepository;

    @Transactional
    public DeviceResponse register(DeviceRegisterRequest request) {
        // UUID가 이미 존재하면 농장 이전만 처리 (카메라 생성 X)
        return deviceRepository.findByDeviceUuid(request.getDeviceUuid())
                .map(existing -> {
                    existing.moveFarm(request.getFarmId());
                    return DeviceResponse.from(existing);
                })
                .orElseGet(() -> {
                    // 신규 등록일 때만 카메라 생성
                    Device device = deviceRepository.save(
                            Device.create(request.getDeviceUuid(), request.getName(), request.getFarmId())
                    );
                    Camera camera = createCameraIfRequested(device, request);
                    return DeviceResponse.from(device, camera);
                });
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

    @Transactional
    public void refreshLastSeenAt(Long deviceId) {
        findById(deviceId).refreshLastSeenAt(OffsetDateTime.now());
    }

    @Transactional
    public void delete(Long deviceId) {
        deviceRepository.delete(findById(deviceId));
    }

    // ─── 패키지 공유 메서드 ───────────────────────────────────────────

    public Device findById(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));
    }

    // ─── private ─────────────────────────────────────────────────────

    private Camera createCameraIfRequested(Device device, DeviceRegisterRequest request) {
        DeviceRegisterRequest.CameraRegisterRequest cameraRequest = request.getCamera();
        if (cameraRequest == null) {
            return null;
        }

        String cameraName = isBlank(cameraRequest.getName())
                ? defaultCameraName(device.getName())
                : cameraRequest.getName().trim();
        String streamKey = isBlank(cameraRequest.getStreamKey())
                ? defaultStreamKey(device)
                : cameraRequest.getStreamKey().trim();
        String streamProtocol = isBlank(cameraRequest.getStreamProtocol())
                ? "HLS"
                : cameraRequest.getStreamProtocol().trim().toUpperCase();
        String captureEndpoint = isBlank(cameraRequest.getCaptureEndpoint())
                ? null
                : cameraRequest.getCaptureEndpoint().trim();
        boolean primary = cameraRequest.getPrimary() == null || cameraRequest.getPrimary();

        Camera camera = Camera.create(
                device,
                cameraName,
                streamKey,
                streamProtocol,
                captureEndpoint,
                primary
        );
        return cameraRepository.save(camera);
    }

    private String defaultCameraName(String deviceName) {
        if (isBlank(deviceName)) {
            return "기본 카메라";
        }
        return deviceName.trim() + " 카메라";
    }

    private String defaultStreamKey(Device device) {
        String uuid = device.getDeviceUuid() == null ? "device" : device.getDeviceUuid().replace("-", "");
        String suffix = uuid.length() <= 8 ? uuid : uuid.substring(0, 8);
        return "farm-" + device.getFarmId() + "-cam-" + suffix;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}