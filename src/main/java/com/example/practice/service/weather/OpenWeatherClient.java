package com.example.practice.service.weather;

import com.example.practice.common.config.OpenWeatherProperties;
import com.example.practice.dto.weather.OpenWeatherOneCallResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OpenWeatherClient {

    private final OpenWeatherProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public OpenWeatherOneCallResponse getHourlyWeather(Double latitude, Double longitude) {
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl() + "/data/3.0/onecall")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("exclude", "current,minutely,daily,alerts")
                .queryParam("units", "metric")
                .queryParam("appid", properties.getApiKey())
                .toUriString();

        OpenWeatherOneCallResponse response =
                restTemplate.getForObject(url, OpenWeatherOneCallResponse.class);

        if (response == null || response.getHourly() == null || response.getHourly().isEmpty()) {
            throw new IllegalStateException("OpenWeather hourly 응답이 비어 있습니다.");
        }

        return response;
    }
}