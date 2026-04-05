package com.example.practice.controller.notification;

import com.example.practice.common.config.TokenAuthFilter;
import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.notification.NotificationResponse;
import com.example.practice.dto.notification.NotificationUnreadCountResponse;
import com.example.practice.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMyNotifications(principal.id(), pageable));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getNotificationCount(
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal principal) {
        long total = notificationService.getTotalCount(principal.id());
        long unread = notificationService.getUnreadCountValue(principal.id()); // ← 직접 long
        return ResponseEntity.ok(Map.of(
                "total", total,
                "unread", unread
        ));
    }

    @GetMapping("/recent-count")
    public ResponseEntity<Map<String, Long>> getRecentCount(
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal principal) {
        long recentCount = notificationService.getRecentCount(principal.id());
        return ResponseEntity.ok(Map.of("recentCount", recentCount));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<NotificationUnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getUnreadCount(principal.id()));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal principal) {
        notificationService.markAsRead(notificationId, principal.id());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal TokenAuthFilter.UserPrincipal principal) {
        notificationService.markAllAsRead(principal.id());
        return ResponseEntity.noContent().build();
    }
}