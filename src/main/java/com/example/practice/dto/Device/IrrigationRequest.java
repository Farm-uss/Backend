package com.example.practice.dto.Device;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IrrigationRequest {

    @NotNull(message = "디바이스 ID는 필수입니다.")
    private Long deviceId;

    @NotNull(message = "명령 타입은 필수입니다.")
    private IrrigationCommand command;

    /**
     * PUMP_ON 시 동작 시간 (초)
     * null이면 PUMP_OFF 명령이 올 때까지 계속 동작
     */
    private Integer durationSeconds;

    public void validate() {
        if (durationSeconds != null && durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds는 1초 이상이어야 합니다.");
        }

        if (command == IrrigationCommand.PUMP_OFF && durationSeconds != null) {
            throw new IllegalArgumentException("PUMP_OFF 요청에는 durationSeconds를 보낼 수 없습니다.");
        }
    }
}