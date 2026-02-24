package com.example.practice.entity.farm;

import com.example.practice.entity.location.Location;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "farm")
public class Farm {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // 농장이 삭제되면 연관된 위치 정보도 고아 객체가 되어 함께 삭제됨
    @Transient
    private Location location;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FarmMember> members = new ArrayList<>();

    private Double area;

    @Enumerated(EnumType.STRING)
    @Column(name = "station_type", nullable = false, length = 10)
    private StationType stationType = StationType.SPOT;

    @Column(name = "obsr_spot_code", length = 20)
    private String obsrSpotCode;

    @Column(name = "zone_code", length = 20)
    private String zoneCode;

    @Column(name = "station_name", length = 50)
    private String stationName;

    @Column(name = "station_updated_at")
    private OffsetDateTime stationUpdatedAt;

}


