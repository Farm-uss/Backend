package com.example.practice.dto.Device;

import com.example.practice.entity.device.Sensor;
import com.example.practice.entity.device.SensorType;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class SensorResponse {

    private Long sensorId;
    private SensorType sensorType;
    private String sensorTypeDescription;
    private String unit;
    private String installLocation;
    private Long deviceId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static SensorResponse from(Sensor sensor) {
        return SensorResponse.builder()
                .sensorId(sensor.getSensorId())
                .sensorType(sensor.getSensorType())
                .sensorTypeDescription(sensor.getSensorType().getDescription())
                .unit(sensor.getUnit())
                .installLocation(sensor.getInstallLocation())
                .deviceId(sensor.getDevice().getDeviceId())
                .createdAt(sensor.getCreatedAt())
                .updatedAt(sensor.getUpdatedAt())
                .build();
    }
}
