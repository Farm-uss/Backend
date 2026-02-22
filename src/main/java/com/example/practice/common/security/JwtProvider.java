package com.example.practice.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component  // ✅ 직접 Bean 등록!
public class JwtProvider {

    @Value("${app.jwt.secret}")  // properties 직접 주입!
    private String secret;

    private final long ACCESS_TOKEN_EXPIRY = 1000L * 60 * 30; // 30분 고정

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(getSigningKey())
                .compact();
    }

    public JwtUser parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return new JwtUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class)
            );
        } catch (Exception e) {
            log.error("JWT 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    public record JwtUser(Long id, String email) {}
}
