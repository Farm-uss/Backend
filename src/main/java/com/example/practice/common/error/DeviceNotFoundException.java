package com.example.practice.common.error;

public class DeviceNotFoundException extends BusinessException {
    public DeviceNotFoundException(Long deviceId) {
        super(ErrorCode.DEVICE_NOT_FOUND, "deviceId: " + deviceId);
    }
}
