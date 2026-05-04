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
import com.example.practice.common.config.KakaoProperties;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final KakaoProperties kakaoProperties;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    private static final int TOKEN_BYTES = 48; // 64~80 chars 정도
    private static final int TOKEN_TTL_DAYS = 7;

    private String exchangeCodeForAccessToken(String code) {
        String tokenUrl = kakaoProperties.getAuthBaseUrl() + "/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoProperties.getClientId());
        body.add("redirect_uri", kakaoProperties.getRedirectUri());
        body.add("code", code);

        if (kakaoProperties.getClientSecret() != null && !kakaoProperties.getClientSecret().isBlank()) {
            body.add("client_secret", kakaoProperties.getClientSecret());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "failed to get kakao access token");
        }

        return responseBody.get("access_token").toString();

    }

    private KakaoUserInfo fetchKakaoUserInfo(String kakaoAccessToken) {
        String userInfoUrl = kakaoProperties.getApiBaseUrl() + "/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "failed to get kakao user info");
        }

        Object idValue = body.get("id");
        if (!(idValue instanceof Number idNumber)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "invalid kakao user info");
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        Map<String, Object> profile = kakaoAccount != null
                ? (Map<String, Object>) kakaoAccount.get("profile")
                : null;

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String nickname = profile != null ? (String) profile.get("nickname") : null;
        String profileImageUrl = profile != null ? (String) profile.get("profile_image_url") : null;

        if (nickname == null || nickname.isBlank()) {
            nickname = "kakao_" + idNumber.longValue();
        }

        return new KakaoUserInfo(
                idNumber.longValue(),
                email,
                nickname,
                profileImageUrl
        );
    }

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

    private User findOrCreateKakaoUser(KakaoUserInfo kakaoUserInfo) {
        String email = kakaoUserInfo.getEmail();

        if (email == null || email.isBlank()) {
            email = "kakao_" + kakaoUserInfo.getId() + "@kakao.local";
        }

        email = email.trim().toLowerCase();

        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            return existingUser;
        }

        String phoneNumber = "KAKAO-" + kakaoUserInfo.getId();
        if (phoneNumber.length() > 20) {
            phoneNumber = phoneNumber.substring(0, 20);
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(generateToken()))
                .nickname(kakaoUserInfo.getNickname())
                .phone_number(phoneNumber)
                .profileImageUrl(kakaoUserInfo.getProfileImageUrl())
                .createdAt(OffsetDateTime.now())
                .build();

        return userRepository.save(user);
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

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse kakaoLogin(KakaoLoginRequest req) {
        if (req == null || req.getCode() == null || req.getCode().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "kakao code is required");
        }

        String kakaoAccessToken = exchangeCodeForAccessToken(req.getCode());

        KakaoUserInfo kakaoUserInfo = fetchKakaoUserInfo(kakaoAccessToken);

        User user = findOrCreateKakaoUser(kakaoUserInfo);

        return issueTokens(user);
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

    private AuthResponse issueTokens(User user) {
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

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getNickname()
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
