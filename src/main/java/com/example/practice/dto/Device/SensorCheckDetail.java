package com.example.practice.dto.Device;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SensorCheckDetail {
    private String  sensorType;  // SensorType enum name
    private String  label;       // 한글 레이블 (조도, 토양 온도 ...)
    private String  unit;        // 단위 (℃, ppm ...)
    private Double  value;       // 실제 측정값
    private Double  min;         // 기준 최솟값
    private Double  max;         // 기준 최댓값
    private boolean inRange;     // 범위 내 여부
}
