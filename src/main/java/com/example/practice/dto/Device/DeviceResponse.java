package com.example.practice.dto.Device;

import com.example.practice.entity.device.Device;
import com.example.practice.entity.device.DeviceStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class DeviceResponse {

    private Long deviceId;
    private String deviceUuid;
    private String name;
    private DeviceStatus status;
    private String statusDescription;
    private OffsetDateTime lastSeenAt;
    private Long farmId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static DeviceResponse from(Device device) {
        return DeviceResponse.builder()
                .deviceId(device.getDeviceId())
                .deviceUuid(device.getDeviceUuid())
                .name(device.getName())
                .status(device.getStatus())
                .statusDescription(device.getStatus() != null ? device.getStatus().getDescription() : null)
                .lastSeenAt(device.getLastSeenAt())
                .farmId(device.getFarmId())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
