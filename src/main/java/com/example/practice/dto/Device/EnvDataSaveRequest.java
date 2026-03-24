package com.example.practice.dto.Device;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class EnvDataSaveRequest {

    @NotNull(message = "장치 ID는 필수입니다.")
    private Long deviceId;

    private BigDecimal temp;          // 토양 온도
    private BigDecimal soilMoisture;  // 토양 수분
    private BigDecimal ph;            // pH
    private BigDecimal illuminance;   // 조도
    private BigDecimal ec;            // EC
    private BigDecimal co2;           // CO2

    /**
     * 라즈베리파이 측정 시각.
     * null이면 서버 수신 시각으로 대체됩니다.
     */
    private OffsetDateTime measuredAt;
}
