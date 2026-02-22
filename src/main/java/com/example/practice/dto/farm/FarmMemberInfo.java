package com.example.practice.dto.farm;

import com.example.practice.entity.farm.FarmRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FarmMemberInfo {
    private Long userId;
    private String userName;
    private FarmRole role;
}
