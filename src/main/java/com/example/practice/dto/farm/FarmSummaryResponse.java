package com.example.practice.dto.farm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FarmSummaryResponse {
    private long totalFarmCount;    // 총 농장 수
    private long ownedFarmCount;    // 내가 만든 농장 수 (OWNER)
    private long joinedFarmCount;   // 초대된 농장 수 (MEMBER)
}