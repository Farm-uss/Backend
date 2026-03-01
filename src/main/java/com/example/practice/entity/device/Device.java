package com.example.practice.entity.device;


import com.example.practice.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
        name = "device",
        indexes = {
                @Index(name = "idx_device_uuid", columnList = "device_uuid", unique = true),
                @Index(name = "idx_device_farm_id", columnList = "farm_id")
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"deviceId", "deviceUuid", "name", "status"})
public class Device extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_uuid", nullable = false, unique = true, length = 36)
    private String deviceUuid;

    @Column(name = "name", length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private DeviceStatus status;

    @Column(name = "last_seen_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastSeenAt;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sensor> sensors = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnvData> envDataList = new ArrayList<>();

    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────

    public static Device create(String deviceUuid, String name, Long farmId) {
        return Device.builder()
                .deviceUuid(deviceUuid)
                .name(name)
                .farmId(farmId)
                .status(DeviceStatus.ACTIVE)
                .build();
    }
    // ─── 비즈니스 메서드 ──────────────────────────────────────────────

    public void changeStatus(DeviceStatus status) {
        this.status = status;
    }

    public void refreshLastSeenAt(OffsetDateTime now) {
        this.lastSeenAt = now;
    }

}
