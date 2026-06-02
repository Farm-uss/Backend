package com.example.practice.service.weather;

import com.example.practice.common.config.OpenWeatherProperties;
import com.example.practice.dto.weather.FarmHourlyWeatherResponseDto;
import com.example.practice.dto.weather.HourlyWeatherItemDto;
import com.example.practice.entity.location.Location;
import com.example.practice.entity.location.WeatherHourly;
import com.example.practice.repository.location.LocationRepository;
import com.example.practice.repository.location.WeatherHourlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FarmWeatherQueryService {

    private final LocationRepository locationRepository;

    private final WeatherHourlyRepository weatherRepository;

    private final WeatherCacheService weatherCacheService;

    private final OpenWeatherProperties openWeatherProperties;

    public FarmHourlyWeatherResponseDto getHourlyWeather(Long farmId) {

        Location location = locationRepository.findByFarmId(farmId)
                .orElseThrow(() -> new IllegalArgumentException("농장 위치 없음"));

        Double lat = location.getLatitude();
        Double lon = location.getLongitude();

        List<WeatherHourly> cached =
                weatherRepository.findByLatitudeAndLongitudeOrderByForecastTimeAsc(lat, lon);

        boolean cacheValid = isCacheValid(cached);

        if (!cacheValid) {

            weatherCacheService.refreshWeatherCache(lat, lon);

            cached = weatherRepository
                    .findByLatitudeAndLongitudeOrderByForecastTimeAsc(lat, lon);
        }

        List<HourlyWeatherItemDto> hourly = cached.stream()
                .limit(12)
                .map(this::toDto)
                .toList();

        return FarmHourlyWeatherResponseDto.builder()
                .farmId(farmId)
                .regionName(location.getRegionName())
                .address(location.getAddress())
                .latitude(lat)
                .longitude(lon)
                .hourlyForecast(hourly)
                .updatedAt(OffsetDateTime.now())
                .cached(cacheValid)
                .build();
    }

    private boolean isCacheValid(List<WeatherHourly> cached) {

        if (cached == null || cached.isEmpty()) {
            return false;
        }

        OffsetDateTime fetchedAt = cached.get(0).getFetchedAt();
        if (fetchedAt == null) {
            return false;
        }

        Long cacheMinutes = openWeatherProperties.getCacheMinutes();
        if (cacheMinutes == null || cacheMinutes <= 0) {
            cacheMinutes = 30L;
        }

        return fetchedAt.isAfter(
                OffsetDateTime.now().minusMinutes(cacheMinutes)
        );
    }


    private HourlyWeatherItemDto toDto(WeatherHourly w) {

        return HourlyWeatherItemDto.builder()
                .forecastTime(w.getForecastTime())
                .label(makeLabel(w.getForecastTime()))
                .displayTime(makeDisplayTime(w.getForecastTime()))
                .temperature(w.getTemperature() == null ? null : (int) Math.round(w.getTemperature()))
                .weather(normalizeWeather(w.getWeatherMain()))
                .weatherText(toKoreanWeather(w.getWeatherMain()))
                .icon(w.getWeatherIcon())
                .build();
    }

    private String makeLabel(OffsetDateTime forecastTime) {
        // 1. 현재 시간과 비교할 대상 시간 모두 한국 시간(KST) 기준으로 맞추기
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        ZonedDateTime target = forecastTime.atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        long diffHours = java.time.Duration.between(now, target).toHours();

        if (diffHours <= 0) {
            return "지금";
        }

        if (diffHours <= 24) {
            return diffHours + "시간 후";
        }

        return makeDisplayTime(forecastTime);
    }

    private String makeDisplayTime(OffsetDateTime forecastTime) {
        // 2. UTC로 들어온 시간을 한국 시간(KST)으로 변환 후 시간(Hour) 추출
        ZonedDateTime kstTime = forecastTime.atZoneSameInstant(ZoneId.of("Asia/Seoul"));
        int hour = kstTime.getHour();

        String period = hour < 12 ? "오전" : "오후";
        int displayHour = hour % 12;

        if (displayHour == 0) {
            displayHour = 12;
        }

        return period + " " + displayHour + "시";
    }

    private String normalizeWeather(String weatherMain) {
        if (weatherMain == null) return "UNKNOWN";

        return switch (weatherMain.toUpperCase()) {
            case "CLEAR" -> "CLEAR";
            case "CLOUDS" -> "CLOUDS";
            case "RAIN", "DRIZZLE", "THUNDERSTORM" -> "RAIN";
            case "SNOW" -> "SNOW";
            default -> "UNKNOWN";
        };
    }

    private String toKoreanWeather(String weatherMain) {
        if (weatherMain == null) return "알 수 없음";

        return switch (weatherMain.toUpperCase()) {
            case "CLEAR" -> "맑음";
            case "CLOUDS" -> "흐림";
            case "RAIN", "DRIZZLE", "THUNDERSTORM" -> "비";
            case "SNOW" -> "눈";
            default -> "알 수 없음";
        };
    }
}