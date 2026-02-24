package com.example.practice.entity.crops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "growth_measurement")
public class GrowthMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "growth_id")
    private Long growthId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crops_id", nullable = false)
    private Crops crops;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    @Column(name = "height", precision = 10, scale = 2)
    private BigDecimal height;

    @Column(name = "leaf_area", precision = 10, scale = 2)
    private BigDecimal leafArea;

    @Column(name = "leaf_count")
    private Integer leafCount;

    @Column(name = "fruit_count")
    private Integer fruitCount;

    @Column(name = "ai_raw_json")
    private String aiRawJson;

    @Column(name = "ai_summary")
    private String aiSummary;

    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
