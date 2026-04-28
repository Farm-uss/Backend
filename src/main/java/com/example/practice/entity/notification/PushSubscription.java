package com.example.practice.entity.notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;


@Entity
@Table(name = "push_subscription")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "p256dh", nullable = false, length = 200)
    private String p256dh;

    @Column(name = "auth", nullable = false, length = 100)
    private String auth;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public static PushSubscription create(Long userId, String endpoint,
                                          String p256dh, String auth) {
        PushSubscription s = new PushSubscription();
        s.userId = userId;
        s.endpoint = endpoint;
        s.p256dh = p256dh;
        s.auth = auth;
        s.createdAt = OffsetDateTime.now();
        return s;
    }
}
