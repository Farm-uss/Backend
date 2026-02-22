package com.example.practice.entity.farm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "farm_invitation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"farm_id", "invited_user_id"}))
public class FarmInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Column(name = "invited_user_id", nullable = false)
    private Long invitedUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}