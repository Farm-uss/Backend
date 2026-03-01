package com.example.practice.dto.Device;

import com.example.practice.entity.device.SensorType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SensorRegisterRequest {

    @NotNull(message = "센서 타입은 필수입니다.")
    private SensorType sensorType;

    @Size(max = 255, message = "단위는 최대 255자입니다.")
    private String unit;  // null이면 SensorType 기본 단위 사용

    @Size(max = 255, message = "설치 위치는 최대 255자입니다.")
    private String installLocation;

    @NotNull(message = "장치 ID는 필수입니다.")
    private Long deviceId;
}
