package com.example.practice.entity.device;
import com.example.practice.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "env_data",
        indexes = {
                @Index(name = "idx_env_data_device_id", columnList = "device_id")
        }
)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"envDataId"})
public class EnvData extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "env_data_id")
    private Long envDataId;

    @Column(name = "temp", precision = 6, scale = 2)
    private BigDecimal temp;

    @Column(name = "humidity", precision = 6, scale = 2)
    private BigDecimal humidity;

    @Column(name = "soil_moisture", precision = 6, scale = 2)
    private BigDecimal soilMoisture;

    @Column(name = "illuminance", precision = 10, scale = 2)
    private BigDecimal illuminance;

    @Column(name = "ec", precision = 8, scale = 4)
    private BigDecimal ec;

    @Column(name = "co2", precision = 8, scale = 2)
    private BigDecimal co2;

    @Column(name = "measured_at")
    private OffsetDateTime measuredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;



    // ─── 정적 팩토리 메서드 ───────────────────────────────────────────

    public static EnvData create(
            BigDecimal temp,           // 1
            BigDecimal humidity,       // 2
            BigDecimal soilMoisture,   // 3
            BigDecimal illuminance,    // 4
            BigDecimal ec,             // 5
            BigDecimal co2,            // 6
            Device device
    ) {
        if (device == null) {
            throw new IllegalArgumentException("device is required");
        }

        return EnvData.builder()
                .temp(temp)
                .humidity(humidity)
                .soilMoisture(soilMoisture)
                .illuminance(illuminance)
                .ec(ec)
                .co2(co2)
                .device(device)
                .measuredAt(OffsetDateTime.now())
                .build();
    }
}
