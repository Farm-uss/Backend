package com.example.practice.dto.weather;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class HourlyWeatherItemDto {

    private OffsetDateTime forecastTime;

    private String label;         // 지금, 1시간 후
    private String displayTime;   // 오후 2시
    private Integer temperature;

    private String weather;
    private String weatherText;

    private String icon;
}