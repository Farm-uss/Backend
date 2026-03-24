package com.example.practice.dto.Device;

import com.example.practice.entity.device.CommandStatus;
import com.example.practice.entity.device.CommandType;
import com.example.practice.entity.device.DeviceCommand;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class CommandResponse {

    private Long commandId;
    private Long deviceId;
    private CommandType commandType;
    private CommandStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime executedAt;

    public static CommandResponse from(DeviceCommand cmd) {
        return CommandResponse.builder()
                .commandId(cmd.getCommandId())
                .deviceId(cmd.getDeviceId())
                .commandType(cmd.getCommandType())
                .status(cmd.getStatus())
                .createdAt(cmd.getCreatedAt())
                .executedAt(cmd.getExecutedAt())
                .build();
    }
}
