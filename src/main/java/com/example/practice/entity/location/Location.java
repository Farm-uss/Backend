package com.example.practice.entity.location;

import com.example.practice.entity.farm.Farm;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "location")
@Data
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    private Double latitude;
    private Double longitude;
    private String address;
    private String regionName;
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "farm_id", nullable = false)
    private Long farmId;
}
