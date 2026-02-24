package com.example.practice.entity.crops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter @Setter
@Entity
@Table(name = "crop_gdd_daily",
        uniqueConstraints = @UniqueConstraint(name = "uk_crop_date", columnNames = {"crops_id", "target_date"}))
public class CropGddDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crops_id", nullable = false)
    private Crops crops;

    @Column(name="target_date", nullable=false)
    private LocalDate targetDate;

    @Column(name = "gdd", precision = 8, scale = 2)
    private BigDecimal gdd;

    @Column(name = "gdd_normal_5y", precision = 8, scale = 2)
    private BigDecimal gddNormal5y;

    @Column(name = "base_temp", precision = 4, scale = 1)
    private BigDecimal baseTemp;

    @Column(name = "station_type", length = 10)
    private String stationType;

    @Column(name = "station_code", length = 20)
    private String stationCode;

    @Column(name = "api_raw_date", length = 8)
    private String apiRawDate;

    @Column(name = "source", length = 30, nullable = false)
    private String source;

    @Column(name = "fetched_at", nullable = false)
    private OffsetDateTime fetchedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}