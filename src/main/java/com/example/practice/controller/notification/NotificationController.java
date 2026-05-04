package com.example.practice.controller.notification;

import com.example.practice.dto.notification.NotificationResponse;
import com.example.practice.dto.notification.NotificationUnreadCountResponse;
import com.example.practice.service.notification.NotificationService;
import com.example.practice.common.config.TokenAuthFilter.UserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Page<NotificationResponse> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return notificationService.getMyNotifications(user.id(), pageable);
    }

    @GetMapping("/unread-count")
    public NotificationUnreadCountResponse getUnreadCount(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return notificationService.getUnreadCount(user.id());
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        notificationService.markAsRead(notificationId, user.id());
    }

    @PatchMapping("/read-all")
    public void markAllAsRead(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        notificationService.markAllAsRead(user.id());
    }
}