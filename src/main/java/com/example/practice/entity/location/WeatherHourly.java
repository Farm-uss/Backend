package com.example.practice.entity.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "weather_hourly")
@Getter
@Setter
@NoArgsConstructor
public class WeatherHourly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weatherHourlyId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private OffsetDateTime forecastTime;

    private Double temperature;

    private String weatherMain;

    private String weatherIcon;

    @Column(nullable = false)
    private OffsetDateTime fetchedAt;
}