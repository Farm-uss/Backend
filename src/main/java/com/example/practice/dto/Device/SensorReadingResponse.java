package com.example.practice.dto.Device;

import com.example.practice.entity.device.SensorReading;
import com.example.practice.entity.device.SensorType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class SensorReadingResponse {

    private Long readingId;
    private BigDecimal value;
    private OffsetDateTime measuredAt;
    private Long sensorId;
    private SensorType sensorType;

    public static SensorReadingResponse from(SensorReading reading) {
        return SensorReadingResponse.builder()
                .readingId(reading.getReadingId())
                .value(reading.getValue())
                .measuredAt(reading.getMeasuredAt())
                .sensorId(reading.getSensor().getSensorId())
                .sensorType(reading.getSensor().getSensorType())
                .build();
    }
}
