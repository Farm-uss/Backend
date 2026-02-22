package com.example.practice.dto.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodeResult {
    @JsonProperty("address_components")
    private List<AddressComponent> addressComponents;

    @JsonProperty("formatted_address")
    private String formattedAddress;

    private Geometry geometry;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressComponent {
        @JsonProperty("long_name")
        private String longName;
        private List<String> types; // administrative_area_level_1 등이 포함됨
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        private LocationPoint location;

        @Data
        public static class LocationPoint {
            private Double lat;
            private Double lng;
        }
    }
}