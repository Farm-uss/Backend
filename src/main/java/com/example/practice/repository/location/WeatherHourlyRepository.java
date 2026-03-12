package com.example.practice.repository.location;

import com.example.practice.entity.location.WeatherHourly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeatherHourlyRepository extends JpaRepository<WeatherHourly, Long> {

    List<WeatherHourly> findByLatitudeAndLongitudeOrderByForecastTimeAsc(Double latitude, Double longitude);

    void deleteByLatitudeAndLongitude(Double latitude, Double longitude);
}