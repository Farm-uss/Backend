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
    private BigDecimal temp;          // 토양 온도
    private BigDecimal soilMoisture;  // 토양 수분
    private BigDecimal ph;            // pH
    private BigDecimal illuminance;   // 조도
    private BigDecimal ec;            // EC
    private BigDecimal co2;           // CO2
    private Long deviceId;
    private OffsetDateTime measuredAt;
    private OffsetDateTime createdAt;

    public static EnvDataResponse from(EnvData envData) {
        return EnvDataResponse.builder()
                .envDataId(envData.getEnvDataId())
                .temp(envData.getTemp())
                .soilMoisture(envData.getSoilMoisture())
                .ph(envData.getPh())
                .illuminance(envData.getIlluminance())
                .ec(envData.getEc())
                .co2(envData.getCo2())
                .deviceId(envData.getDevice().getDeviceId())
                .measuredAt(envData.getMeasuredAt())
                .createdAt(envData.getCreatedAt())
                .build();
    }
}
