package com.example.practice.dto.Device;

import com.example.practice.entity.device.CommandStatus;
import com.example.practice.entity.device.DeviceCommand;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class IrrigationResponse {

    private Long commandId;
    private Long deviceId;
    private String commandType;
    private Integer durationSeconds;
    private CommandStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime executedAt;

    public static IrrigationResponse from(DeviceCommand cmd) {
        return IrrigationResponse.builder()
                .commandId(cmd.getCommandId())
                .deviceId(cmd.getDeviceId())
                .commandType(cmd.getCommandType().name())
                .durationSeconds(cmd.getDurationSeconds())
                .status(cmd.getStatus())
                .createdAt(cmd.getCreatedAt())
                .executedAt(cmd.getExecutedAt())
                .build();
    }
}