package com.example.practice.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {
    private Long id;
    private String email;
    private String nickname;
    private String phone_number;
    private String profileImageUrl;

}
