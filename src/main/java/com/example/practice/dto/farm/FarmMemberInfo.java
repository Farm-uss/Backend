package com.example.practice.dto.farm;

import com.example.practice.entity.farm.FarmRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FarmMemberInfo {
    private Long userId;
    private String userName;
    private FarmRole role;
    private String profileImg;

}
