package com.example.practice.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmHourlyWeatherResponseDto {
    private Long farmId;
    private String regionName;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<HourlyWeatherItemDto> hourlyForecast;
    private OffsetDateTime updatedAt;
    private Boolean cached;
}