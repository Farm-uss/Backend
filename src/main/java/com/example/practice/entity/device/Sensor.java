package com.example.practice.entity.device;

import com.example.practice.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "sensor",
        indexes = {
                @Index(name = "idx_sensor_device_id", columnList = "device_id"),
                @Index(name = "idx_sensor_device_type", columnList = "device_id, sensor_type", unique = true)
        }
)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"sensorId", "sensorType"})
public class Sensor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_id")
    private Long sensorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 50)
    private SensorType sensorType;

    @Column(name = "unit", length = 255)
    private String unit;

    @Column(name = "install_location", length = 255)
    private String installLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SensorReading> readings = new ArrayList<>();

    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────

    public static Sensor create(SensorType sensorType, Device device,
                                String unit, String installLocation) {
        return Sensor.builder()
                .sensorType(sensorType)
                .device(device)
                .unit(unit != null ? unit : sensorType.getDefaultUnit())
                .installLocation(installLocation)
                .build();
    }
}
