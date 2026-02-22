package com.example.practice.dto.farm;

import com.example.practice.entity.farm.Farm;
import com.example.practice.entity.farm.FarmRole;
import com.example.practice.entity.location.Location;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class FarmResponse {

    private Long id;
    private String name;
    // Location 엔티티 대신 필요한 정보만 필드로 선언
    private String address;
    private String img;
    private Double latitude;
    private Double longitude;
    private String myRole;
    private OffsetDateTime createdAt;
    private Double area;

    public static FarmResponse from(Farm farm, FarmRole role) {
        // Location이 null일 경우를 대비한 안전한 처리
        String address = (farm.getLocation() != null) ? farm.getLocation().getAddress() : null;
        Double lat = (farm.getLocation() != null) ? farm.getLocation().getLatitude() : null;
        Double lng = (farm.getLocation() != null) ? farm.getLocation().getLongitude() : null;

        return FarmResponse.builder()
                .id(farm.getId())
                .name(farm.getName())
                .address(address)
                .latitude(lat)
                .longitude(lng)
                .createdAt(farm.getCreatedAt())
                .myRole(role.name())
                .area(farm.getArea())
                .img(farm.getImagePath())
                .build();
    }
}

