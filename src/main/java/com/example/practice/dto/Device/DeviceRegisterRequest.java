package com.example.practice.dto.Device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceRegisterRequest {

    @NotBlank(message = "UUID는 필수입니다.")
    @Size(max = 36, message = "UUID는 최대 36자입니다.")
    private String deviceUuid;

    @Size(max = 255, message = "장치 이름은 최대 255자입니다.")
    private String name;

    @NotNull(message = "농장 ID는 필수입니다.")
    private Long farmId;
}
