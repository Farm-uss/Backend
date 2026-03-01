package com.example.practice.dto.Device;

import com.example.practice.entity.device.EnvData;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class EnvDataResponse {

    private Long envDataId;
    private BigDecimal temp;
    private BigDecimal humidity;
    private BigDecimal soilMoisture;
    private BigDecimal illuminance;
    private BigDecimal ec;
    private BigDecimal co2;
    private Long deviceId;
    private OffsetDateTime createdAt;

    public static EnvDataResponse from(EnvData envData) {
        return EnvDataResponse.builder()
                .envDataId(envData.getEnvDataId())
                .temp(envData.getTemp())
                .humidity(envData.getHumidity())
                .soilMoisture(envData.getSoilMoisture())
                .illuminance(envData.getIlluminance())
                .ec(envData.getEc())
                .co2(envData.getCo2())
                .deviceId(envData.getDevice().getDeviceId())
                .createdAt(envData.getCreatedAt())
                .build();
    }
}
