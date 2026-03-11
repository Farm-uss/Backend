package com.example.practice.dto.user;

import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Schema(description = "회원가입 요청 DTO")
public class SignUpRequest {

    @Schema(description = "이메일", example = "test@test.com")
    private String email;

    @Schema(description = "비밀번호", example = "1234qwer!")
    private String password;


    @Schema(description = "이름", example = "홍길동")
    private String nickname;


    @Schema(description = "전화번호", example = "010-1111-1111")
    private String phoneNumber;

    private List<String> profileImageIds;
}
