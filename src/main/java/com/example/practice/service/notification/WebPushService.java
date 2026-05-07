package com.example.practice.service.notification;

import com.example.practice.entity.notification.PushSubscription;
import com.example.practice.repository.notification.PushSubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

@Slf4j
@Service
public class WebPushService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushService pushService;
    private final String subject;

    public WebPushService(
            PushSubscriptionRepository pushSubscriptionRepository,
            @Value("${vapid.public-key}") String publicKey,
            @Value("${vapid.private-key}") String privateKey,
            @Value("${vapid.subject}") String subject
    ) throws GeneralSecurityException {

        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.subject = subject;

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        this.pushService = new PushService(publicKey, privateKey, subject);
    }

    public void sendToUser(Long userId, String title, String body) {
        List<PushSubscription> subscriptions =
                pushSubscriptionRepository.findAllByUserId(userId);

        log.info("[WebPush] pushService instance id={}, subject={}",
                System.identityHashCode(pushService), subject);

        subscriptions.forEach(sub -> {
            try {
                String payload = String.format(
                        "{\"title\":\"%s\",\"body\":\"%s\"}",
                        escapeJson(title), escapeJson(body)
                );

                // ↓ 수정 - URL-safe Base64로 변환 후 사용
                String p256dh = toUrlSafeBase64(sub.getP256dh());
                String auth = toUrlSafeBase64(sub.getAuth());

                Subscription subscription = new Subscription(
                        sub.getEndpoint(),
                        new Subscription.Keys(p256dh, auth)
                );

                String endpoint = sub.getEndpoint();
                URI uri = URI.create(endpoint);
                String endpointOrigin = uri.getScheme() + "://" + uri.getHost();

                log.info("[WebPush] send start userId={}, endpoint={}, origin={}, pushServiceId={}",
                        userId, endpoint, endpointOrigin, System.identityHashCode(pushService));

                Notification notification = new Notification(subscription, payload);
                HttpResponse response = pushService.send(notification);

                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = response.getEntity() != null
                        ? EntityUtils.toString(response.getEntity())
                        : "";

                log.info("[WebPush] send end userId={}, origin={}, statusCode={}, body={}, pushServiceId={}",
                        userId, endpointOrigin, statusCode, responseBody,
                        System.identityHashCode(pushService));

                if (statusCode == 404 || statusCode == 410) {
                    log.warn("[WebPush] expired subscription deleted userId={}, endpoint={}", userId, endpoint);
                    pushSubscriptionRepository.deleteByEndpoint(endpoint);
                }

            } catch (Exception e) {
                log.error("[WebPush] send fail userId={}, endpoint={}, pushServiceId={}, message={}",
                        userId, sub.getEndpoint(), System.identityHashCode(pushService), e.getMessage(), e);
            }
        });
    }

    // ↓ 추가
    private String toUrlSafeBase64(String base64) {
        if (base64 == null) return null;
        return base64
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "");
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}