package com.example.practice.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "프로필 이미지 변경 요청 DTO")
public class ProfileImageUpdateRequest {

    @Schema(description = "프로필 이미지 ID", example = "4")
    private String image;
}
