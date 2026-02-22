package com.example.practice.entity.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 200)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column( nullable = false, length = 20) // NOT NULL!
    private String phone_number;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;


}
