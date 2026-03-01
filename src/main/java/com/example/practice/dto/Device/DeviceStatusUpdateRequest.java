package com.example.practice.dto.Device;

import com.example.practice.entity.device.DeviceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceStatusUpdateRequest {

    @NotNull(message = "상태 값은 필수입니다.")
    private DeviceStatus status;
}
