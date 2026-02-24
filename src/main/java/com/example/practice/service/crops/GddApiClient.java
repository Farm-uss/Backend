package com.example.practice.service.crops;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GddApiClient {

    private final WebClient webClient;

    public GddApiClient(WebClient.Builder builder,
                        @Value("${gdd.api.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<String> fetchSpotXml(String serviceKey, String obsrSpotCode, String beginDate, String endDate, String cropCode, String growthTempIf99) {
        return webClient.get()
                .uri(uri -> {
                    var b = uri.path("/getWeatherDegreeDaySpotList")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("obsr_Spot_Code", obsrSpotCode)
                            .queryParam("begin_Date", beginDate)
                            .queryParam("end_Date", endDate)
                            .queryParam("growth_Temp_Crop_Code", cropCode);
                    if ("99".equals(cropCode) && growthTempIf99 != null) b.queryParam("growth_Temp", growthTempIf99);
                    return b.build();
                })
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> fetchZoneXml(String serviceKey, String zoneCode, String beginDate, String endDate, String cropCode, String growthTempIf99) {
        return webClient.get()
                .uri(uri -> {
                    var b = uri.path("/getWeatherDegreeDayList")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("zone_Code", zoneCode)
                            .queryParam("begin_Date", beginDate)
                            .queryParam("end_Date", endDate)
                            .queryParam("growth_Temp_Crop_Code", cropCode);
                    if ("99".equals(cropCode) && growthTempIf99 != null) b.queryParam("growth_Temp", growthTempIf99);
                    return b.build();
                })
                .retrieve()
                .bodyToMono(String.class);
    }
}