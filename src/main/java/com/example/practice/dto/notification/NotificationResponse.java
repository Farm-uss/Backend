package com.example.practice.dto.notification;

import com.example.practice.entity.notification.Notification;
import com.example.practice.entity.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;

@Getter @Builder
public class NotificationResponse {
    private Long id;
    private Long farmId;
    private String message;
    private NotificationType type;
    private String typeDescription;
    private boolean isRead;
    private OffsetDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .farmId(n.getFarmId())
                .message(n.getMessage())
                .type(n.getType())
                .typeDescription(n.getType().getDescription())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}