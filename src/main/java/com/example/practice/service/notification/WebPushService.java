package com.example.practice.service.notification;

import com.example.practice.entity.notification.PushSubscription;
import com.example.practice.repository.notification.PushSubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

@Slf4j
@Service
public class WebPushService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushService pushService;

    public WebPushService(
            PushSubscriptionRepository pushSubscriptionRepository,
            @Value("${vapid.public-key}") String publicKey,
            @Value("${vapid.private-key}") String privateKey,
            @Value("${vapid.subject}") String subject) throws GeneralSecurityException {

        this.pushSubscriptionRepository = pushSubscriptionRepository;

        // BouncyCastle 등록
        Security.addProvider(new BouncyCastleProvider());

        this.pushService = new PushService(publicKey, privateKey, subject);
    }

    public void sendToUser(Long userId, String title, String body) {
        List<PushSubscription> subscriptions =
                pushSubscriptionRepository.findAllByUserId(userId);

        subscriptions.forEach(sub -> {
            try {
                String payload = String.format(
                        "{\"title\":\"%s\",\"body\":\"%s\"}", title, body);

                // ECPublicKey 변환 없이 Subscription 객체로 바로 처리
                nl.martijndwars.webpush.Subscription subscription =
                        new nl.martijndwars.webpush.Subscription(
                                sub.getEndpoint(),
                                new nl.martijndwars.webpush.Subscription.Keys(
                                        sub.getP256dh(),
                                        sub.getAuth()
                                )
                        );

                Notification notification = new Notification(subscription, payload);
                pushService.send(notification);

            } catch (Exception e) {
                log.error("[WebPush] 발송 실패 userId={}: {}", userId, e.getMessage());
            }
        });
    }

}