package com.example.practice.entity.device;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Entity
@Table(
        name = "sensor_reading",
        indexes = {
                @Index(name = "idx_reading_sensor_id", columnList = "sensor_id"),
                @Index(name = "idx_reading_measured_at", columnList = "measured_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"readingId", "value", "measuredAt"})
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long readingId;

    @Column(name = "value", precision = 12, scale = 4)
    private BigDecimal value;

    @Column(name = "measured_at", nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime measuredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────

    public static SensorReading create(
            BigDecimal value,
            OffsetDateTime measuredAt,
            Sensor sensor
    ) {
        if (value == null) {
            throw new IllegalArgumentException("value is required");
        }
        if (sensor == null) {
            throw new IllegalArgumentException("sensor is required");
        }

        return SensorReading.builder()
                .value(value)
                .measuredAt(measuredAt != null ? measuredAt : OffsetDateTime.now())
                .sensor(sensor)
                .build();
    }
}
