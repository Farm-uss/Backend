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

    // 목록 조회 - Pageable의 sort를 살리려면 OrderByCreatedAtDesc를 빼는 게 좋음
    Page<Notification> findAllByUserId(Long userId, Pageable pageable);

    // 단건 읽음 처리용 - 본인 알림만 조회
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsRead(Long userId, boolean isRead);

    List<Notification> findAllByUserIdAndIsReadFalse(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Notification n
           SET n.isRead = true
         WHERE n.userId = :userId
           AND n.isRead = false
    """)
    int markAllAsRead(@Param("userId") Long userId);

    Optional<Notification> findTopByFarmIdAndTypeOrderByCreatedAtDesc(
            Long farmId, NotificationType type);

    @Query("""
        SELECT COUNT(n)
          FROM Notification n
         WHERE n.userId = :userId
           AND n.createdAt >= :from
    """)
    long countRecentNotifications(@Param("userId") Long userId,
                                  @Param("from") OffsetDateTime from);

    long countByUserId(Long userId);

    void deleteAllByFarmId(Long farmId);
}
