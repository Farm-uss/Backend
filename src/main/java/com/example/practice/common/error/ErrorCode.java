package com.example.practice.common.error;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Device
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "존재하지 않는 Device입니다."),
    DEVICE_UUID_DUPLICATE(HttpStatus.CONFLICT, "D002", "이미 등록된 UUID입니다."),

    // Sensor
    SENSOR_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 Sensor입니다."),
    SENSOR_TYPE_DUPLICATE(HttpStatus.CONFLICT, "S002", "이미 등록된 센서 타입입니다."),

    // SensorReading
    SENSOR_READING_NOT_FOUND(HttpStatus.NOT_FOUND, "SR001", "존재하지 않는 SensorReading입니다."),

    // EnvData
    ENV_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "존재하지 않는 EnvData입니다."),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}

