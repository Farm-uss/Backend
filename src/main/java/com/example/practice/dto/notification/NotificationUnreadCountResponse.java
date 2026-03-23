package com.example.practice.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationUnreadCountResponse {
    private long unreadCount;
}