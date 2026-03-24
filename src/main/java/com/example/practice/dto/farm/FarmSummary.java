package com.example.practice.dto.farm;

import com.example.practice.entity.farm.FarmRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FarmSummary {
    private Long farmId;
    private String name;
    private String location;    // 주소

    private Double area;

    private String ownerName;   // 농장 주인 이름
    private int memberCount;     // OWNER 제외 멤버 수
    private List<FarmMemberInfo> members; // 멤버들


    private String img;
    private List<String> crops; // 작물 이름 리스트
    private String cropCode;
    private Long cropsId;
    private FarmRole role;      // 내 역할
    private OffsetDateTime createdDate; // 생성 날짜
}