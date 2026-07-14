package com.vzap.trytons.service;

import com.vzap.trytons.dto.NotificationCreateRequestDTO;
import com.vzap.trytons.dto.NotificationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponseDTO> getNotificationsForUser(UUID actorUserId, boolean unreadOnly);
    int getUnreadCount(UUID actorUserId);
    NotificationResponseDTO markAsRead(UUID actorUserId, UUID notificationId);
    int markAllAsRead(UUID actorUserId);

    NotificationResponseDTO createNotification(NotificationCreateRequestDTO request);
}
