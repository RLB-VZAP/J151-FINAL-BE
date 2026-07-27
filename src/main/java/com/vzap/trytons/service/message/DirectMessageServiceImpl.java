package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.message.DirectMessageDAO;
import com.vzap.trytons.dao.message.MessageReportDAO;
import com.vzap.trytons.dao.message.MessageRequestDAO;
import com.vzap.trytons.dao.message.UserBlockDAO;
import com.vzap.trytons.dto.message.ConversationThreadDTO;
import com.vzap.trytons.dto.message.DirectMessageResponseDTO;
import com.vzap.trytons.dto.message.SendDirectMessageRequestDTO;
import com.vzap.trytons.dto.notification.NotificationCreateRequestDTO;
import com.vzap.trytons.enums.DirectMessageStatus;
import com.vzap.trytons.enums.MessageScope;
import com.vzap.trytons.enums.NotificationType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
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
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectMessageServiceImpl implements DirectMessageService {

    private static final Logger LOG = Logger.getLogger(DirectMessageServiceImpl.class.getName());

    private static final int PREVIEW_LENGTH = 120;

    /** Rule F bound: the reported message plus this many immediately preceding it. Never more. */
    private static final int ADMIN_PRECEDING_MESSAGE_COUNT = 10;

    @Inject
    private DirectMessageDAO directMessageDAO;
    @Inject
    private UserBlockDAO userBlockDAO;
    @Inject
    private MessageRequestDAO messageRequestDAO;
    @Inject
    private MessageReportDAO messageReportDAO;
    @Inject
    private MessageFilterService messageFilterService;
    @Inject
    private UserDAO userDAO;
    @Inject
    private NotificationService notificationService;
    @Inject
    private DeviceTokenService deviceTokenService;
    @Inject
    private FcmService fcmService;

    @Override
    public DirectMessageResponseDTO send(UUID actorUserId, SendDirectMessageRequestDTO request) {
        User sender = requireAuthenticated(actorUserId);
        String body = requireMessageBody(actorUserId, request);
        UUID recipientUserId = request.getRecipientUserId();
        User recipient = requireActiveRecipient(recipientUserId);

        if (userBlockDAO.existsEitherDirection(actorUserId, recipientUserId)) {
            throw new AuthorisationException("You cannot send messages to this user.");
        }
        requireConsent(actorUserId, recipientUserId);

        DirectMessage created = persistMessage(actorUserId, recipientUserId, body);
        if (created.getStatus() == DirectMessageStatus.REJECTED) {
            // Rule D: stored for admin review, but the recipient must never see it.
            throw new BusinessRuleException("Your message was not delivered because it contains blocked content.");
        }

        notifyRecipient(recipientUserId, created, sender);
        pushToRecipient(recipientUserId, created, sender);
        return mapToResponse(created, actorUserId);
    }

    private String requireMessageBody(UUID actorUserId, SendDirectMessageRequestDTO request) {
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
        return body;
    }

    private User requireActiveRecipient(UUID recipientUserId) {
        User recipient = userDAO.getUserById(recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found."));
        if (!Boolean.TRUE.equals(recipient.getIsActive())) {
            throw new ResourceNotFoundException("Recipient not found.");
        }
        return recipient;
    }

    private DirectMessage persistMessage(UUID actorUserId, UUID recipientUserId, String body) {
        Optional<String> blockedPhrase = messageFilterService.firstBlockedPhrase(body);
        DirectMessage message = DirectMessage.builder()
                .senderUserId(actorUserId)
                .recipientUserId(recipientUserId)
                .body(body)
                .isRead(false)
                .status(blockedPhrase.isPresent() ? DirectMessageStatus.REJECTED : DirectMessageStatus.APPROVED)
                .build();
        return directMessageDAO.create(message);
    }

    /**
     * Rule A/B/C: a league in common never grants private-chat access. Sending
     * requires either an APPROVED message request in either direction, or an
     * already-existing (grandfathered) conversation between the pair.
     */
    private void requireConsent(UUID actorUserId, UUID recipientUserId) {
        boolean hasApprovedRequest = messageRequestDAO.existsApprovedEitherDirection(actorUserId, recipientUserId);
        boolean hasExistingConversation = directMessageDAO.existsApprovedMessageBetween(actorUserId, recipientUserId);
        if (!hasApprovedRequest && !hasExistingConversation) {
            throw new BusinessRuleException(
                    "You need this user's permission before you can message them. Please send a message request first.");
        }
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

    @Override
    public List<DirectMessageResponseDTO> getAdminWindow(UUID adminUserId, UUID reportedMessageId) {
        requireAuthenticated(adminUserId);
        if (reportedMessageId == null) {
            throw new ValidationException("A reported message is required.");
        }

        DirectMessage anchor = directMessageDAO.findById(reportedMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found."));
        if (!messageReportDAO.existsReportForMessage(MessageScope.DIRECT, reportedMessageId)) {
            throw new AuthorisationException("This conversation has no reported message; admin access is denied.");
        }

        List<DirectMessage> window = directMessageDAO.findAdminWindow(
                anchor.getSenderUserId(), anchor.getRecipientUserId(),
                anchor.getCreatedAt(), anchor.getMessageId(), ADMIN_PRECEDING_MESSAGE_COUNT);

        List<DirectMessageResponseDTO> response = new ArrayList<>();
        for (DirectMessage message : window) {
            response.add(mapToResponse(message, adminUserId));
        }
        return response;
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
                .status(message.getStatus())
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
