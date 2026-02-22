package com.example.practice.repository.user;

import com.example.practice.entity.user.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("""
        select at from AuthToken at
        join fetch at.user
        where at.token = :token
    """)
    Optional<AuthToken> findByTokenWithUser(@Param("token") String token);
}
