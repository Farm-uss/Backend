package com.example.practice.service.notification;

import com.example.practice.dto.notification.PushSubscriptionRequest;
import com.example.practice.entity.notification.PushSubscription;
import com.example.practice.repository.notification.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Value("${vapid.public-key}")
    private String publicKey;

    public void subscribe(Long userId, PushSubscriptionRequest request) {
        PushSubscription subscription = PushSubscription.create(
                userId,
                request.getEndpoint(),
                request.getP256dh(),
                request.getAuth()
        );
        pushSubscriptionRepository.save(subscription);
    }

    public void unsubscribe(String endpoint) {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }

    public String getPublicKey() {
        return publicKey;
    }
}