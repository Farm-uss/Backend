
package com.example.practice.dto.crops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CropCodeResponse {
    private String cropCode;
    private String cropName;
    private boolean exists;
}
