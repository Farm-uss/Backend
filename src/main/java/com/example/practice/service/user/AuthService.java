package com.example.practice.service.user;

import com.example.practice.entity.user.AuthToken;
import com.example.practice.entity.user.User;
import com.example.practice.repository.user.AuthTokenRepository;
import com.example.practice.repository.user.UserRepository;
import com.example.practice.common.error.AppException;
import com.example.practice.common.security.JwtProvider;
import com.example.practice.dto.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final JwtProvider jwtProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    private static final int TOKEN_BYTES = 48; // 64~80 chars 정도
    private static final int TOKEN_TTL_DAYS = 7;

    @Transactional
    public void signup(SignUpRequest req) {
        if (req == null || req.getEmail() == null || req.getEmail().trim().isEmpty()
                || req.getPassword() == null || req.getPassword().trim().isEmpty()
                || req.getNickname() == null || req.getNickname().trim().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "email/password/nickname are required");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, "email already exists");
        }
        if (req.getPhoneNumber() == null || req.getPhoneNumber().trim().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "phoneNumber is required");
        }

        String profileImageUrl = buildS3ImageUrl(req.getProfileImageIds());

        User user = User.builder()
                .email(req.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname().trim())
                .phone_number(req.getPhoneNumber())
                .profileImageUrl(profileImageUrl)
                .createdAt(OffsetDateTime.now())
                .build();

        userRepository.save(user);
    }

    private String buildS3ImageUrl(List<String> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return null;

        String firstId = imageIds.get(0).trim();
        if (!firstId.matches("^[0-9]{1,10}$")) {  // 1~10자리 숫자만
            throw new AppException(HttpStatus.BAD_REQUEST, "invalid image ID");
        }

        return String.format("https://hansungfarmimg.s3.eu-north-1.amazonaws.com/user/%s.png", firstId);
    }


    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }

        String refreshToken = generateToken();
        OffsetDateTime now = OffsetDateTime.now();

        AuthToken authToken = AuthToken.builder()
                .token(refreshToken)
                .user(user)
                .createdAt(now)
                .expiresAt(now.plusDays(TOKEN_TTL_DAYS))
                .build();
        authTokenRepository.save(authToken);

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());

        Long userId = user.getId();
        String nickname = user.getNickname();

        return new AuthResponse(accessToken, refreshToken, userId, nickname);
    }


    @Transactional(readOnly = true)
    public User authenticateByToken(String token) {
        if (token == null || token.isBlank()) return null;

        AuthToken authToken = authTokenRepository
                .findByTokenWithUser(token)
                .orElse(null);

        if (authToken == null) return null;

        if (authToken.getExpiresAt().isBefore(OffsetDateTime.now())) return null;

        return authToken.getUser(); // 이제 Lazy 예외 안 남
    }


    @Transactional(readOnly = true)
    public RefreshResponse refresh(RefreshRequest req) {
        if (req == null || req.getRefreshToken() == null || req.getRefreshToken().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "refreshToken is required");
        }

        AuthToken authToken = authTokenRepository.findByTokenWithUser(req.getRefreshToken())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "invalid refresh token"));

        if (authToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "refresh token expired");
        }

        User user = authToken.getUser();
        String newAccess = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        return new RefreshResponse(newAccess);
    }

    @Transactional
    public void logout(LogoutRequest req) {
        if (req == null || req.getRefreshToken() == null || req.getRefreshToken().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "refreshToken is required");
        }

        // 토큰이 없더라도 "로그아웃 완료"처럼 처리하고 싶으면 orElseThrow 말고 deleteByToken 써도 됨
        AuthToken token = authTokenRepository.findByTokenWithUser(req.getRefreshToken())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "invalid refresh token"));

        authTokenRepository.delete(token);
    }

    @Transactional
    public String updateProfileImage(Long userId, String imageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "사용자 없음"));

        String imageUrl = buildProfileImageUrl(imageId);
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }


    @Transactional(readOnly = true)
    public MeResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "사용자 없음"));

        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPhone_number(),
                user.getProfileImageUrl()
        );
    }



    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        // URL-safe
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildProfileImageUrl(String imageId) {
        if (imageId == null || imageId.trim().isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "image is required");
        }
        return buildS3ImageUrl(Collections.singletonList(imageId));
    }
}
