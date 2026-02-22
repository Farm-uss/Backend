package com.example.practice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter

@Schema(description = "로그인 요청 DTO")
public class LoginRequest {

    @Schema(description = "이메일", example = "admin@smartfarm.kr")
    private String email;


    @Schema(description = "비밀번호", example = "password")
    private String password;
}
