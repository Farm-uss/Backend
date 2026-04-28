package com.example.practice.repository.notification;

import com.example.practice.entity.notification.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushSubscriptionRepository
        extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findAllByUserId(Long userId);
    void deleteByEndpoint(String endpoint);
}