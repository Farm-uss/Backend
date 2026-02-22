package com.example.practice.dto.farm;

import com.example.practice.entity.farm.InvitationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class InvitationResponse {

    private Long invitationId;
    private Long farmId;
    private String farmName;
    private InvitationStatus status;
    private OffsetDateTime createdAt;
}