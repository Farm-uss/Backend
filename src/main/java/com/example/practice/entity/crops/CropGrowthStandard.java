package com.example.practice.entity.crops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@Entity
@Table(
        name = "crop_growth_standard",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_crop_growth_standard_crop_code", columnNames = "crop_code")
        }
)
public class CropGrowthStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crop_name")
    private String cropName;

    @Column(name = "base_temperature")
    private Double baseTemperature;

    @Column(name = "target_gdd_germination")
    private Integer targetGddGermination;

    @Column(name = "target_gdd_flowering")
    private Integer targetGddFlowering;

    @Column(name = "target_gdd_harvest")
    private Integer targetGddHarvest;

    @Column(name = "crop_code", length = 2)
    private String cropCode;

    @Column(name = "base_temp", precision = 4, scale = 1)
    private BigDecimal baseTemp;

    @Column(name = "target_gdd", precision = 10, scale = 2)
    private BigDecimal targetGdd;
}