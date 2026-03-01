package com.example.practice.dto.Device;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class SensorReadingSaveRequest {

    @NotNull(message = "센서 ID는 필수입니다.")
    private Long sensorId;

    @NotNull(message = "측정값은 필수입니다.")
    private BigDecimal value;

    // null이면 서버 수신 시각으로 자동 설정
    private OffsetDateTime measuredAt;
}
