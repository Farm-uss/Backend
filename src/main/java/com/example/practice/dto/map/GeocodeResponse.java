package com.example.practice.dto.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodeResponse {
    private List<GeocodeResult> results;
    private String status; // OK, ZERO_RESULTS 등
}


