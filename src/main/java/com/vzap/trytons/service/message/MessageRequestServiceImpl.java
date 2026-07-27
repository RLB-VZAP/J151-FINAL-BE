package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.message.MessageRequestDAO;
import com.vzap.trytons.dao.message.UserBlockDAO;
import com.vzap.trytons.dto.message.CreateMessageRequestDTO;
import com.vzap.trytons.dto.message.MessageRequestOverviewDTO;
import com.vzap.trytons.dto.message.MessageRequestResponseDTO;
import com.vzap.trytons.dto.notification.NotificationCreateRequestDTO;
import com.vzap.trytons.enums.MessageRequestStatus;
import com.vzap.trytons.enums.NotificationType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.message.MessageRequest;
import com.vzap.trytons.service.notification.NotificationService;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageRequestServiceImpl implements MessageRequestService {

    private static final Logger LOG = Logger.getLogger(MessageRequestServiceImpl.class.getName());

    private static final String MESSAGE_REQUEST_ENTITY_TYPE = "MESSAGE_REQUEST";

    @Inject
    private MessageRequestDAO messageRequestDAO;
    @Inject
    private UserBlockDAO userBlockDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private NotificationService notificationService;

    @Override
    public MessageRequestResponseDTO createRequest(UUID actorUserId, CreateMessageRequestDTO request) {
        requireAuthenticated(actorUserId);
        UUID targetUserId = requireTargetUser(actorUserId, request);

        User target = userDAO.getUserById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found."));
        if (!Boolean.TRUE.equals(target.getIsActive())) {
            throw new ResourceNotFoundException("Target user not found.");
        }
        if (userBlockDAO.existsEitherDirection(actorUserId, targetUserId)) {
            throw new AuthorisationException("You cannot send a message request to this user.");
        }

        MessageRequest created = resolveOrCreatePending(actorUserId, targetUserId);
        notifyTargetOfRequest(target, actorUserId, created);
        return mapToResponse(created);
    }

    @Override
    public MessageRequestOverviewDTO listRequests(UUID actorUserId) {
        requireAuthenticated(actorUserId);

        List<MessageRequestResponseDTO> incoming = messageRequestDAO.findIncoming(actorUserId).stream()
                .map(this::mapToResponse).toList();
        List<MessageRequestResponseDTO> outgoing = messageRequestDAO.findOutgoing(actorUserId).stream()
                .map(this::mapToResponse).toList();

        return MessageRequestOverviewDTO.builder().incoming(incoming).outgoing(outgoing).build();
    }

    @Override
    public MessageRequestResponseDTO approve(UUID actorUserId, UUID requestId) {
        MessageRequest updated = respondToRequest(actorUserId, requestId, MessageRequestStatus.APPROVED);
        notifyRequesterOfApproval(updated);
        return mapToResponse(updated);
    }

    @Override
    public MessageRequestResponseDTO reject(UUID actorUserId, UUID requestId) {
        MessageRequest updated = respondToRequest(actorUserId, requestId, MessageRequestStatus.REJECTED);
        return mapToResponse(updated);
    }

    private UUID requireTargetUser(UUID actorUserId, CreateMessageRequestDTO request) {
        if (request == null || request.getTargetUserId() == null) {
            throw new ValidationException("A target user is required.");
        }
        if (actorUserId.equals(request.getTargetUserId())) {
            throw new ValidationException("You cannot request to message yourself.");
        }
        return request.getTargetUserId();
    }

    /**
     * Rule respects the unique (requester, target) key: a pair only ever has one
     * request row. A REJECTED row is reopened to PENDING rather than duplicated,
     * so a user can always ask again after being turned down.
     */
    private MessageRequest resolveOrCreatePending(UUID requesterUserId, UUID targetUserId) {
        return messageRequestDAO.findByPair(requesterUserId, targetUserId)
                .map(this::reopenIfRejectedOtherwiseThrow)
                .orElseGet(() -> messageRequestDAO.create(MessageRequest.builder()
                        .requesterUserId(requesterUserId)
                        .targetUserId(targetUserId)
                        .status(MessageRequestStatus.PENDING)
                        .build()));
    }

    private MessageRequest reopenIfRejectedOtherwiseThrow(MessageRequest existing) {
        if (existing.getStatus() == MessageRequestStatus.PENDING) {
            throw new ConflictException("You already have a pending request to this user.");
        }
        if (existing.getStatus() == MessageRequestStatus.APPROVED) {
            throw new ConflictException("You already have permission to message this user.");
        }
        messageRequestDAO.reopenAsPending(existing.getRequestId());
        return messageRequestDAO.findById(existing.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Message request not found."));
    }

    private MessageRequest respondToRequest(UUID actorUserId, UUID requestId, MessageRequestStatus decision) {
        requireAuthenticated(actorUserId);
        if (requestId == null) {
            throw new ValidationException("A request is required.");
        }

        MessageRequest request = messageRequestDAO.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Message request not found."));
        if (!actorUserId.equals(request.getTargetUserId())) {
            throw new AuthorisationException("Only the recipient of a request may respond to it.");
        }
        if (request.getStatus() != MessageRequestStatus.PENDING) {
            throw new BusinessRuleException("This request has already been responded to.");
        }

        LocalDateTime respondedAt = LocalDateTime.now();
        messageRequestDAO.updateStatus(requestId, decision, respondedAt);
        request.setStatus(decision);
        request.setRespondedAt(respondedAt);
        return request;
    }

    private void notifyTargetOfRequest(User target, UUID requesterUserId, MessageRequest request) {
        try {
            String requesterUsername = userDAO.getUserById(requesterUserId).map(User::getUsername).orElse("Someone");
            NotificationCreateRequestDTO notification = NotificationCreateRequestDTO.builder()
                    .userId(target.getUserId())
                    .type(NotificationType.CHAT_MESSAGE)
                    .body(requesterUsername + " wants to send you a message.")
                    .relatedEntityType(MESSAGE_REQUEST_ENTITY_TYPE)
                    .relatedEntityId(request.getRequestId())
                    .build();
            notificationService.createNotification(notification);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Unable to notify target user of new message request", e);
        }
    }

    private void notifyRequesterOfApproval(MessageRequest request) {
        try {
            String targetUsername = userDAO.getUserById(request.getTargetUserId())
                    .map(User::getUsername).orElse("The user");
            NotificationCreateRequestDTO notification = NotificationCreateRequestDTO.builder()
                    .userId(request.getRequesterUserId())
                    .type(NotificationType.CHAT_MESSAGE)
                    .body(targetUsername + " approved your message request.")
                    .relatedEntityType(MESSAGE_REQUEST_ENTITY_TYPE)
                    .relatedEntityId(request.getRequestId())
                    .build();
            notificationService.createNotification(notification);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Unable to notify requester of message request approval", e);
        }
    }

    private MessageRequestResponseDTO mapToResponse(MessageRequest request) {
        String requesterUsername = userDAO.getUserById(request.getRequesterUserId())
                .map(User::getUsername).orElse("Unknown user");
        String targetUsername = userDAO.getUserById(request.getTargetUserId())
                .map(User::getUsername).orElse("Unknown user");

        return MessageRequestResponseDTO.builder()
                .requestId(request.getRequestId())
                .requesterUserId(request.getRequesterUserId())
                .requesterUsername(requesterUsername)
                .targetUserId(request.getTargetUserId())
                .targetUsername(targetUsername)
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .build();
    }

    private void requireAuthenticated(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated user is required.");
        }
        User user = userDAO.getUserById(actorUserId)
                .orElseThrow(() -> new AuthorisationException("An authenticated user is required."));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthorisationException("An authenticated user is required.");
        }
    }
}
