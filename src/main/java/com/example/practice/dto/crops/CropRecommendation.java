package com.example.practice.dto.crops;

import com.example.practice.dto.Device.SensorCheckDetail;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class CropRecommendation {
    private String cropCode;      // crop_environment_standard.crop_code
    private int    matchedCount;  // 범위 내 센서 수
    private int    totalChecked;  // 비교한 센서 수
    private double matchRate;     // 매칭율 (%)
    private List<SensorCheckDetail> details;
}
