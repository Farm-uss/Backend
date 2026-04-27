package com.example.practice.controller.notification;

import com.example.practice.common.config.TokenAuthFilter;
import com.example.practice.dto.notification.PushSubscriptionRequest;
import com.example.practice.service.notification.PushSubscriptionService;
import com.example.practice.service.notification.WebPushService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;
    private final WebPushService webPushService ;

    // 브라우저 구독 정보 저장
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal principal,
            @RequestBody PushSubscriptionRequest request) {
        pushSubscriptionService.subscribe(principal.id(), request);
        return ResponseEntity.ok().build();
    }

    // 구독 취소
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @RequestBody PushSubscriptionRequest request) {
        pushSubscriptionService.unsubscribe(request.getEndpoint());
        return ResponseEntity.noContent().build();
    }

    // 프론트에서 VAPID Public Key 가져갈 때 사용
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of(
                "publicKey", pushSubscriptionService.getPublicKey()
        ));
    }

    // PushSubscriptionController에 임시 추가
    @PostMapping("/test")
    public ResponseEntity<Void> test(
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal principal) {
        webPushService.sendToUser(principal.id(), "농장 환경 알림", "테스트 알림입니다!");
        return ResponseEntity.ok().build();
    }
}