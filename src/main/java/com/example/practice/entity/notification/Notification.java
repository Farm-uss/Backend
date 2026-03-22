package com.example.practice.entity.notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "notification",
        indexes = {
                @Index(name = "idx_notification_user_id", columnList = "user_id"),
                @Index(name = "idx_notification_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    public static Notification create(Long userId, Long farmId,
                                      String message, NotificationType type) {
        Notification n = new Notification();
        n.userId = userId;
        n.farmId = farmId;
        n.message = message;
        n.type = type;
        n.isRead = false;
        n.createdAt = OffsetDateTime.now();
        return n;
    }

    public void markAsRead() { this.isRead = true; }
}