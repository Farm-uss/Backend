package com.example.practice.service.notification;

import com.example.practice.dto.notification.NotificationResponse;
import com.example.practice.dto.notification.NotificationUnreadCountResponse;
import com.example.practice.entity.notification.Notification;
import com.example.practice.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository
                .findAllByUserId(userId, pageable)
                .map(NotificationResponse::from);
    }

    public NotificationUnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        return new NotificationUnreadCountResponse(count);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 본인 알림이 아닙니다."));
        notification.markAsRead();
    }

    public long getTotalCount(Long userId) {
        return notificationRepository.countByUserId(userId);
    }

    public long getUnreadCountValue(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    public long getRecentCount(Long userId) {
        OffsetDateTime tenMinutesAgo = OffsetDateTime.now().minusMinutes(10);
        return notificationRepository.countRecentNotifications(userId, tenMinutesAgo);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}