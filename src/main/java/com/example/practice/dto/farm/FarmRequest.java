package com.example.practice.dto.farm;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data //Lombok getter/setter
public class FarmRequest {
    @NotBlank(message = "농장 이름은 필수입니다")
    @Size(max = 100, message = "농장 이름은 100자 이하입니다")
    private String name;

    @NotNull(message = "면적은 필수입니다")
    @Positive(message = "면적은 0보다 커야 합니다")
    private Double area;

    @NotBlank @Size(max = 200)
    private String address;  // 구글 API로 변환될 주소



    @NotBlank @Size(max = 100)
    private String cropName;  // 작물 이름
}
