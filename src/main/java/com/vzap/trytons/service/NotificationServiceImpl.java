package com.vzap.trytons.service;

import com.vzap.trytons.dao.NotificationDAO;
import com.vzap.trytons.dao.UserDAO;
import com.vzap.trytons.dto.NotificationCreateRequestDTO;
import com.vzap.trytons.dto.NotificationResponseDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.Notification;
import com.vzap.trytons.model.User;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NotificationServiceImpl implements NotificationService{

    @Inject
    private NotificationDAO notificationDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public List<NotificationResponseDTO> getNotificationsForUser(UUID actorUserId, boolean unreadOnly) {
        requireAuthenticated(actorUserId);

        List<Notification> notifications;
        if(Boolean.TRUE.equals(unreadOnly)){
            notifications = notificationDAO.findByUserIdAndReadStatus(actorUserId, false);
        }else{
            notifications = notificationDAO.findByUserId(actorUserId);
        }

        List<NotificationResponseDTO> response = new ArrayList<>();
        for(Notification notification : notifications){
            response.add(mapToResponse(notification));
        }
        return response;
    }

    @Override
    public int getUnreadCount(UUID actorUserId) {
        requireAuthenticated(actorUserId);
        return notificationDAO.countUnreadByUserId(actorUserId);
    }

    @Override
    public NotificationResponseDTO markAsRead(UUID actorUserId, UUID notificationId) {
        requireAuthenticated(actorUserId);

        if(notificationId == null){
            throw new ValidationException("No Notification Id was found");
        }

        Notification notification = notificationDAO.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if(notification.getUser() == null || !actorUserId.equals(notification.getUser().getUserId())){
            throw new AuthorisationException("User may only mark their own messages as read");
        }

        notificationDAO.markAsRead(notificationId);
        notification.setIsRead(true);
        return mapToResponse(notification);
    }

    @Override
    public int markAllAsRead(UUID actorUserId) {
        requireAuthenticated(actorUserId);
        return notificationDAO.countUnreadByUserId(actorUserId);
    }

    @Override
    public NotificationResponseDTO createNotification(NotificationCreateRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Notification details are required.");
        }
        if (request.getUserId() == null) {
            throw new ValidationException("A target user is required.");
        }
        if (request.getType() == null) {
            throw new ValidationException("A notification type is required.");
        }
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new ValidationException("Notification body is required.");
        }

        userDAO.getUserById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found."));

        Notification notification = new Notification();
        notification.setUser(User.builder().userId(request.getUserId()).build());
        notification.setType(request.getType());
        notification.setBody(request.getBody());
        notification.setIsRead(false);
        notification.setRelatedEntityType(request.getRelatedEntityType());
        notification.setRelatedEntityId(request.getRelatedEntityId());

        Notification created = notificationDAO.create(notification);
        return mapToResponse(created);
    }
    private void requireAuthenticated(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated user is required.");
        }
        Optional<User> user = userDAO.getUserById(actorUserId);
        if (user.isEmpty() || !Boolean.TRUE.equals(user.get().getIsActive())) {
            throw new AuthorisationException("An authenticated user is required.");
        }
    }

    private NotificationResponseDTO mapToResponse(Notification notification) {
        return NotificationResponseDTO.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .body(notification.getBody())
                .createdAt(notification.getCreatedAt())
                .isRead(Boolean.TRUE.equals(notification.getIsRead()))
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .build();
    }
}
