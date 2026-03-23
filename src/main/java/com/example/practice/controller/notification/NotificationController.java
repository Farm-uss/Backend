package com.example.practice.controller.notification;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.notification.NotificationResponse;
import com.example.practice.dto.notification.NotificationUnreadCountResponse;
import com.example.practice.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications - 내 알림 목록
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getMyNotifications(
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(notificationService.getMyNotifications(userId, pageable));
    }

    // GET /api/notifications/unread-count - 읽지 않은 알림 수 (뱃지용)
    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadCountResponse> getUnreadCount(
            @RequestAttribute("userId") Long userId) {
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    // PATCH /api/notifications/{id}/read - 단건 읽음 처리
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        notificationService.markAsRead(id, userId);
        return ApiResponse.success();
    }

    // PATCH /api/notifications/read-all - 전체 읽음 처리
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.success();
    }
}