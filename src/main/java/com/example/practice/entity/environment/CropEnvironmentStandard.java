package com.example.practice.entity.environment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "crop_environment_standard",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_crop_environment_standard_crop_code", columnNames = "crop_code")
        }
)
public class CropEnvironmentStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * CropGrowthStandard.cropCode 와 연결해서 사용
     */
    @Column(name = "crop_code", nullable = false, length = 10)
    private String cropCode;

    @Column(name = "min_temp")
    private Double minTemp;

    @Column(name = "max_temp")
    private Double maxTemp;

    @Column(name = "min_ph")
    private Double minPh;

    @Column(name = "max_ph")
    private Double maxPh;

    @Column(name = "min_soil_moisture")
    private Double minSoilMoisture;

    @Column(name = "max_soil_moisture")
    private Double maxSoilMoisture;

    @Column(name = "min_co2")
    private Double minCo2;

    @Column(name = "max_co2")
    private Double maxCo2;

    @Column(name = "min_ec")
    private Double minEc;

    @Column(name = "max_ec")
    private Double maxEc;

    @Column(name = "min_light")
    private Double minLight;

    @Column(name = "max_light")
    private Double maxLight;
}