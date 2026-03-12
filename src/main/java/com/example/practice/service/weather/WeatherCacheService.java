package com.example.practice.service.weather;

import com.example.practice.dto.weather.OpenWeatherOneCallResponse;
import com.example.practice.entity.location.WeatherHourly;
import com.example.practice.repository.location.WeatherHourlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherCacheService {

    private final WeatherHourlyRepository weatherRepository;

    private final OpenWeatherClient openWeatherClient;

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshWeatherCache(Double lat, Double lon) {

        OpenWeatherOneCallResponse response =
                openWeatherClient.getHourlyWeather(lat, lon);

        weatherRepository.deleteByLatitudeAndLongitude(lat, lon);

        OffsetDateTime fetchedAt = OffsetDateTime.now();

        List<WeatherHourly> toSave = response.getHourly()
                .stream()
                .limit(24)
                .map(hour -> {

                    WeatherHourly w = new WeatherHourly();

                    w.setLatitude(lat);
                    w.setLongitude(lon);

                    w.setForecastTime(
                            Instant.ofEpochSecond(hour.getDt())
                                    .atZone(SEOUL)
                                    .toOffsetDateTime()
                    );

                    w.setTemperature(hour.getTemp());

                    w.setWeatherMain(
                            hour.getWeather().get(0).getMain()
                    );

                    w.setWeatherIcon(
                            hour.getWeather().get(0).getIcon()
                    );

                    w.setFetchedAt(fetchedAt);

                    return w;
                })
                .toList();

        weatherRepository.saveAll(toSave);
    }
}