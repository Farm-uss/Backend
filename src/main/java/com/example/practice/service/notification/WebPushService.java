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

        log.info("[WebPush] pushService instance id={}, subject={}",
                System.identityHashCode(pushService),
                "mailto:0509tkddnr@naver.com");

        subscriptions.forEach(sub -> {
            try {
                String payload = String.format(
                        "{\"title\":\"%s\",\"body\":\"%s\"}", title, body);

                nl.martijndwars.webpush.Subscription subscription =
                        new nl.martijndwars.webpush.Subscription(
                                sub.getEndpoint(),
                                new nl.martijndwars.webpush.Subscription.Keys(
                                        sub.getP256dh(),
                                        sub.getAuth()
                                )
                        );

                String endpoint = sub.getEndpoint();
                String endpointOrigin = java.net.URI.create(endpoint).getScheme()
                        + "://"
                        + java.net.URI.create(endpoint).getHost();

                log.info("[WebPush] send start userId={}, endpoint={}, origin={}, pushServiceId={}",
                        userId, endpoint, endpointOrigin, System.identityHashCode(pushService));

                Notification notification = new Notification(subscription, payload);
                var response = pushService.send(notification);

                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = response.getEntity() != null
                        ? org.apache.http.util.EntityUtils.toString(response.getEntity())
                        : "";

                log.info("[WebPush] send end userId={}, origin={}, statusCode={}, body={}, pushServiceId={}",
                        userId, endpointOrigin, statusCode, responseBody,
                        System.identityHashCode(pushService));

            } catch (Exception e) {
                log.error("[WebPush] send fail userId={}, endpoint={}, pushServiceId={}, message={}",
                        userId, sub.getEndpoint(), System.identityHashCode(pushService), e.getMessage(), e);
            }
        });
    }

}