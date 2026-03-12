package com.example.practice.dto.weather;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OpenWeatherOneCallResponse {

    private List<Hourly> hourly;

    @Getter
    @NoArgsConstructor
    public static class Hourly {
        private Long dt;
        private Double temp;
        private List<Weather> weather;
    }

    @Getter
    @NoArgsConstructor
    public static class Weather {
        private String main;
        private String description;
        private String icon;
    }
}