package com.example.practice.dto.Device;

import com.example.practice.entity.device.SensorType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class SensorValueSnapshot {
    private Double soilTemperature;
    private Double ph;
    private Double soilMoisture;
    private Double co2;
    private Double ec;
    private Double illuminance;
    private Double soilHumidity;   // 기준 컬럼 없어서 추천 비교에선 미사용

    public static SensorValueSnapshot from(Map<SensorType, BigDecimal> values) {
        return SensorValueSnapshot.builder()
                .soilTemperature(toDouble(values.get(SensorType.SOIL_TEMPERATURE)))
                .ph(toDouble(values.get(SensorType.PH)))
                .soilMoisture(toDouble(values.get(SensorType.SOIL_MOISTURE)))
                .co2(toDouble(values.get(SensorType.CO2)))
                .ec(toDouble(values.get(SensorType.EC)))
                .illuminance(toDouble(values.get(SensorType.ILLUMINANCE)))
                .soilHumidity(toDouble(values.get(SensorType.SOIL_HUMIDITY)))
                .build();
    }

    private static Double toDouble(BigDecimal v) {
        return v != null ? v.doubleValue() : null;
    }
}
