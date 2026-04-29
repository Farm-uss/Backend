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
import java.util.List;
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
    public ResponseEntity<String> uploadProfileImage(
            Authentication authentication,
            @RequestBody ProfileImageUpdateRequest req) {

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String imageUrl = authService.updateProfileImage(principal.id(), req.getImage());

        return ResponseEntity.ok(imageUrl);
    }

    @Operation(summary = "사용자 검색", description = "닉네임 검색")
    @GetMapping("/search")
    public List<UserSearchDto> searchUsers(@RequestParam String nickname) {
        return userService.searchUsers(nickname);
    }


}
