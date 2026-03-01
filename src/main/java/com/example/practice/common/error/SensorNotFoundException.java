package com.example.practice.common.error;

public class SensorNotFoundException extends BusinessException {
    public SensorNotFoundException(Long sensorId) {
        super(ErrorCode.SENSOR_NOT_FOUND, "sensorId: " + sensorId);
    }
}
