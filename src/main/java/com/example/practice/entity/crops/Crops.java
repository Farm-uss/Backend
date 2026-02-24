package com.example.practice.entity.crops;

import com.example.practice.entity.farm.Farm;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@Entity
@Table(name = "crops")
public class Crops {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crops_id")
    private Long cropsId;

    @Column(name = "name")
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Column(name = "accumulated_gdd")
    private Double accumulatedGdd;


    @Column(name = "current_stage")
    private String currentStage;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_standard_id")
    private CropGrowthStandard growthStandard;

    @Column(name = "planting_date")
    private LocalDate plantingDate;


    @Column(name = "target_gdd", precision = 10, scale = 2)
    private BigDecimal targetGdd;

    @Column(name = "base_temp", precision = 4, scale = 1)
    private BigDecimal baseTemp;
}