package com.example.practice.repository.notification;

import com.example.practice.entity.notification.Notification;
import com.example.practice.entity.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndIsRead(Long userId, boolean isRead);
    List<Notification> findAllByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    // ↓ 추가 - 농장+타입 기준 가장 최근 알림 1건 조회
    Optional<Notification> findTopByFarmIdAndTypeOrderByCreatedAtDesc(
            Long farmId, NotificationType type);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :from")
    long countRecentNotifications(@Param("userId") Long userId,
                                  @Param("from") OffsetDateTime from);



    // ↓ 추가 - 전체 알림 수 조회
    long countByUserId(Long userId);
}