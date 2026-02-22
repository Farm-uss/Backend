package com.example.practice.service.farm;

import com.example.practice.dto.map.GeocodeResponse;
import com.example.practice.dto.map.GeocodeResult;
import com.example.practice.entity.location.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 생성
public class LocationService {
    private final RestTemplate restTemplate;

    @Value("${google.map.api-key}")
    private String googleApiKey;

    public Location geocodeAddress(String address) {

        // 1. UriComponentsBuilder를 사용해 URI를 생성 (중복 인코딩 방지)
        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("address", address)
                .queryParam("region", "kr")
                .queryParam("key", googleApiKey)
                .build()
                .encode() // 여기서 딱 한 번만 인코딩합니다.
                .toUri();

        // GeocodeResponse 등 결과 DTO는 별도로 정의되어 있어야 합니다.
        ResponseEntity<GeocodeResponse> response = restTemplate.getForEntity(uri, GeocodeResponse.class);

        // 로그 추가: 구글이 보내준 상태 코드가 무엇인지 확인
        System.out.println("Final Request URL: " + uri);
        System.out.println("Google API Status: " + response.getBody().getStatus());

        if (response.getBody() == null || response.getBody().getResults().isEmpty()) {
            throw new RuntimeException("주소를 찾을 수 없습니다. 상태코드: " + response.getBody().getStatus());
        }

        GeocodeResult result = response.getBody().getResults().get(0);

        Location location = new Location();
        location.setAddress(address);
        location.setLatitude(result.getGeometry().getLocation().getLat());
        location.setLongitude(result.getGeometry().getLocation().getLng());


        String region = result.getAddressComponents().stream()
                .filter(c -> c.getTypes().contains("administrative_area_level_1"))
                .findFirst()
                .map(GeocodeResult.AddressComponent::getLongName) // 클래스명 명시
                .orElse("");
        location.setRegionName(region);

        return location;
    }
}