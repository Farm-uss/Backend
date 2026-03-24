package com.example.practice.dto.Device;

import com.example.practice.entity.device.CommandType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommandRequest {

    @NotNull(message = "명령 타입은 필수입니다.")
    private CommandType commandType;   // LED_ON or LED_OFF
}
