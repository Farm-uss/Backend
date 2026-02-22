package com.example.practice.controller.user;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.service.user.AuthService;
import com.example.practice.common.config.TokenAuthFilter.UserPrincipal;
import com.example.practice.common.error.AppException;
import com.example.practice.dto.user.*;
import com.example.practice.service.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입 / 로그인 API")
public class AuthController { //whduddnqkqh

    private final AuthService authService;
    private final UserService userService;


    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody SignUpRequest req) {
        authService.signup(req);
    }


    @Operation(summary = "로그인", description = "그냥 그대로 로그인 시도하면 됩니다")
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }



    @Operation(summary = "마이페이지", description = "그냥 토큰만 넣으면 됩니다")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(Authentication authentication) {  // ApiResponse 추가!

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // AuthService.me() 호출해서 최신 DB 데이터 가져오기!
        MeResponse meResponse = authService.me(principal.id());



        return ResponseEntity.ok(ApiResponse.success(meResponse));
    }
    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody LogoutRequest req) {
        authService.logout(req);
    }


    @Operation(summary = "프로필 변경", description = "프로필 사진을 변경합니다.")
    @PostMapping("/profile/image")
    public ResponseEntity<?> uploadProfileImage(
            Authentication authentication,
            @RequestParam("image") MultipartFile imageFile) {

        // 1. 현재 로그인한 사용자 ID 가져오기
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.id();

        try {
            // 2. 고유 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // 3. uploads/profile 폴더에 저장
            Path uploadDir = Paths.get("uploads/profile");
            Files.createDirectories(uploadDir);  // 폴더 없으면 생성

            Path filePath = uploadDir.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. DB에 URL 업데이트 (AuthService에 updateProfileImage 메서드 추가 필요)
            String imageUrl = "/uploads/profile/" + fileName;
            authService.updateProfileImage(userId, imageUrl);

            // 5. 성공 응답
            return ResponseEntity.ok().body(imageUrl);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("이미지 업로드 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "사용자 검색", description = "닉네임 검색")
    @GetMapping("/search")
    public List<UserSearchDto> searchUsers(@RequestParam String nickname) {
        return userService.searchUsers(nickname);
    }


}
