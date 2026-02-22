package com.example.practice.dto.farm;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteRequest {
    @NotNull
    private Long invitedUserId;
}