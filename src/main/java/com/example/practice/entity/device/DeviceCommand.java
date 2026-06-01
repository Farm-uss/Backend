package com.example.practice.entity.device;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "device_command",
        indexes = {
                @Index(name = "idx_command_device_status", columnList = "device_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "command_id")
    private Long commandId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", nullable = false, length = 20)
    private CommandType commandType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommandStatus status;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @Column(name = "executed_at",
            columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime executedAt;

    // ─── 정적 팩토리 ─────────────────────────────────────────────

    public static DeviceCommand create(Long deviceId, CommandType commandType) {
        DeviceCommand cmd = new DeviceCommand();
        cmd.deviceId = deviceId;
        cmd.commandType = commandType;
        cmd.status = CommandStatus.PENDING;
        cmd.createdAt = OffsetDateTime.now();
        return cmd;
    }

    // ─── 비즈니스 메서드 ──────────────────────────────────────────

    public void markExecuted() {
        this.status = CommandStatus.EXECUTED;
        this.executedAt = OffsetDateTime.now();
    }

    public void markFailed() {
        this.status = CommandStatus.FAILED;
        this.executedAt = OffsetDateTime.now();
    }
}
