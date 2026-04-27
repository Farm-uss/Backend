package com.example.practice.dto.crops;

import com.example.practice.dto.Device.SensorValueSnapshot;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class CropRecommendResponse {
    private Long                     deviceId;
    private SensorValueSnapshot sensorSnapshot;   // 비교에 사용된 현재 센서값
    private int                      minMatchRequired; // 최소 매칭 기준 (3)
    private List<CropRecommendation> recommendations;
    private String                   message;
}