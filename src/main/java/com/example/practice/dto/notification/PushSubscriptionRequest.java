package com.example.practice.dto.notification;

import lombok.Getter;

@Getter
public class PushSubscriptionRequest {
    private String endpoint;
    private String p256dh;
    private String auth;
}