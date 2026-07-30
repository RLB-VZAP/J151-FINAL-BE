package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.message.DirectMessageDAO;
import com.vzap.trytons.dao.message.UserBlockDAO;
import com.vzap.trytons.dto.message.ConversationThreadDTO;
import com.vzap.trytons.dto.message.DirectMessageResponseDTO;
import com.vzap.trytons.dto.message.SendDirectMessageRequestDTO;
import com.vzap.trytons.dto.notification.NotificationCreateRequestDTO;
import com.vzap.trytons.enums.NotificationType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.message.ConversationThread;
import com.vzap.trytons.model.message.DirectMessage;
import com.vzap.trytons.service.device.DeviceTokenService;
import com.vzap.trytons.service.notification.FcmService;
import com.vzap.trytons.service.notification.NotificationService;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DirectMessageServiceImpl implements DirectMessageService {

    private static final Logger LOG = Logger.getLogger(DirectMessageServiceImpl.class.getName());

    private static final int PREVIEW_LENGTH = 120;

    @Inject
    private DirectMessageDAO directMessageDAO;
    @Inject
    private UserBlockDAO userBlockDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private MessageRequestService messageRequestService;
    @Inject
    private NotificationService notificationService;
    @Inject
    private DeviceTokenService deviceTokenService;
    @Inject
    private FcmService fcmService;

    @Override
    public DirectMessageResponseDTO send(UUID actorUserId, SendDirectMessageRequestDTO request) {
        User sender = requireAuthenticated(actorUserId);

        if (request == null) {
            throw new ValidationException("Message details are required.");
        }
        if (request.getRecipientUserId() == null) {
            throw new ValidationException("A recipient is required.");
        }
        String body = request.getBody() == null ? "" : request.getBody().trim();
        if (body.isEmpty()) {
            throw new ValidationException("Message body is required.");
        }
        if (actorUserId.equals(request.getRecipientUserId())) {
            throw new ValidationException("You cannot message yourself.");
        }

        UUID recipientUserId = request.getRecipientUserId();
        User recipient = userDAO.getUserById(recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found."));
        if (!Boolean.TRUE.equals(recipient.getIsActive())) {
            throw new ResourceNotFoundException("Recipient not found.");
        }

        if (userBlockDAO.existsEitherDirection(actorUserId, recipientUserId)) {
            throw new AuthorisationException("You cannot send messages to this user.");
        }

        // Direct messages need the recipient's consent: one side asks, the other
        // accepts, and only then may either send. League chat is the unapproved
        // channel — membership of the league is the permission there, so it goes
        // through LeagueMessageService and never reaches this check.
        if (!messageRequestService.canExchangeMessages(actorUserId, recipientUserId)) {
            throw new AuthorisationException(
                    "You need an accepted message request before you can message " + recipient.getUsername() + ".");
        }

        DirectMessage message = DirectMessage.builder()
                .senderUserId(actorUserId)
                .recipientUserId(recipientUserId)
                .body(body)
                .isRead(false)
                .build();
        DirectMessage created = directMessageDAO.create(message);

        notifyRecipient(recipientUserId, created, sender);
        pushToRecipient(recipientUserId, created, sender);

        return mapToResponse(created, actorUserId);
    }

    @Override
    public List<ConversationThreadDTO> getThreads(UUID actorUserId) {
        requireAuthenticated(actorUserId);

        List<ConversationThreadDTO> threads = new ArrayList<>();
        for (ConversationThread thread : directMessageDAO.findThreads(actorUserId)) {
            threads.add(ConversationThreadDTO.builder()
                    .counterpartUserId(thread.getCounterpartUserId())
                    .counterpartUsername(thread.getCounterpartUsername())
                    .lastMessageBody(thread.getLastMessageBody())
                    .lastMessageAt(thread.getLastMessageAt())
                    .unreadCount(thread.getUnreadCount())
                    .build());
        }
        return threads;
    }

    @Override
    public List<DirectMessageResponseDTO> getConversation(UUID actorUserId, UUID counterpartUserId, LocalDateTime since) {
        requireAuthenticated(actorUserId);
        if (counterpartUserId == null) {
            throw new ValidationException("A conversation partner is required.");
        }

        List<DirectMessageResponseDTO> messages = new ArrayList<>();
        for (DirectMessage message : directMessageDAO.findConversation(actorUserId, counterpartUserId, since)) {
            messages.add(mapToResponse(message, actorUserId));
        }
        return messages;
    }

    @Override
    public int markThreadRead(UUID actorUserId, UUID counterpartUserId) {
        requireAuthenticated(actorUserId);
        if (counterpartUserId == null) {
            throw new ValidationException("A conversation partner is required.");
        }
        return directMessageDAO.markThreadRead(actorUserId, counterpartUserId);
    }

    @Override
    public int getUnreadCount(UUID actorUserId) {
        requireAuthenticated(actorUserId);
        return directMessageDAO.countUnread(actorUserId);
    }

    private void notifyRecipient(UUID recipientUserId, DirectMessage message, User sender) {
        try {
            NotificationCreateRequestDTO request = NotificationCreateRequestDTO.builder()
                    .userId(recipientUserId)
                    .type(NotificationType.CHAT_MESSAGE)
                    .body("New message from " + sender.getUsername())
                    .relatedEntityType("DIRECT_MESSAGE")
                    .relatedEntityId(message.getMessageId())
                    .build();
            notificationService.createNotification(request);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Unable to create in-app notification for direct message", e);
        }
    }

    private void pushToRecipient(UUID recipientUserId, DirectMessage message, User sender) {
        List<String> tokens = deviceTokenService.activeTokensForUser(recipientUserId);
        fcmService.send(tokens,
                "New message from " + sender.getUsername(),
                preview(message.getBody()),
                Map.of(
                        "type", NotificationType.CHAT_MESSAGE.name(),
                        "messageId", message.getMessageId().toString(),
                        "senderUserId", sender.getUserId().toString()));
    }

    private String preview(String body) {
        if (body.length() <= PREVIEW_LENGTH) {
            return body;
        }
        return body.substring(0, PREVIEW_LENGTH) + "…";
    }

    private DirectMessageResponseDTO mapToResponse(DirectMessage message, UUID actorUserId) {
        return DirectMessageResponseDTO.builder()
                .messageId(message.getMessageId())
                .senderUserId(message.getSenderUserId())
                .recipientUserId(message.getRecipientUserId())
                .body(message.getBody())
                .createdAt(message.getCreatedAt())
                .isRead(Boolean.TRUE.equals(message.getIsRead()))
                .mine(actorUserId.equals(message.getSenderUserId()))
                .build();
    }

    private User requireAuthenticated(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated user is required.");
        }
        User user = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated user is required."));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthorisationException("An authenticated user is required.");
        }
        return user;
    }
}
