package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.message.MessageRequestDAO;
import com.vzap.trytons.dao.message.UserBlockDAO;
import com.vzap.trytons.dto.message.CreateMessageRequestRequestDTO;
import com.vzap.trytons.dto.message.MessageContactDTO;
import com.vzap.trytons.dto.message.MessageRequestResponseDTO;
import com.vzap.trytons.dto.notification.NotificationCreateRequestDTO;
import com.vzap.trytons.enums.MessageContactState;
import com.vzap.trytons.enums.MessageRequestStatus;
import com.vzap.trytons.enums.NotificationType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.message.MessageRequest;
import com.vzap.trytons.service.notification.NotificationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MessageRequestServiceImpl implements MessageRequestService {

    private static final Logger LOG = Logger.getLogger(MessageRequestServiceImpl.class.getName());

    private static final int INTRO_MAX_LENGTH = 255;

    @Inject
    private MessageRequestDAO messageRequestDAO;
    @Inject
    private UserBlockDAO userBlockDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private NotificationService notificationService;

    @Override
    public List<MessageRequestResponseDTO> listIncoming(UUID actorUserId) {
        requireAuthenticated(actorUserId);
        List<MessageRequestResponseDTO> requests = new ArrayList<>();
        for (MessageRequest request : messageRequestDAO.findPendingForAddressee(actorUserId)) {
            requests.add(toResponse(request, actorUserId));
        }
        return requests;
    }

    @Override
    public List<MessageRequestResponseDTO> listOutgoing(UUID actorUserId) {
        requireAuthenticated(actorUserId);
        List<MessageRequestResponseDTO> requests = new ArrayList<>();
        for (MessageRequest request : messageRequestDAO.findPendingFromRequester(actorUserId)) {
            requests.add(toResponse(request, actorUserId));
        }
        return requests;
    }

    @Override
    public MessageRequestResponseDTO create(UUID actorUserId, CreateMessageRequestRequestDTO request) {
        User requester = requireAuthenticated(actorUserId);

        if (request == null || request.getAddresseeUserId() == null) {
            throw new ValidationException("A user to request is required.");
        }
        UUID addresseeUserId = request.getAddresseeUserId();
        if (actorUserId.equals(addresseeUserId)) {
            throw new ValidationException("You cannot request to message yourself.");
        }

        User addressee = activeUserOrNotFound(addresseeUserId);

        if (userBlockDAO.existsEitherDirection(actorUserId, addresseeUserId)) {
            throw new AuthorisationException("You cannot message this user.");
        }

        String introMessage = trimIntro(request.getIntroMessage());

        // An accepted request either way means they can already talk, and a
        // pending one from the other side should be answered rather than
        // mirrored — otherwise two people asking each other would deadlock on
        // two PENDING rows that neither reads as actionable.
        Optional<MessageRequest> existing = messageRequestDAO.findBetween(actorUserId, addresseeUserId);
        if (existing.isPresent()) {
            MessageRequest current = existing.get();
            if (current.getStatus() == MessageRequestStatus.ACCEPTED) {
                throw new BusinessRuleException("You can already message this user.");
            }
            if (current.getStatus() == MessageRequestStatus.PENDING) {
                if (current.getAddresseeUserId().equals(actorUserId)) {
                    throw new BusinessRuleException(
                            addressee.getUsername() + " has already asked to message you. Accept their request instead.");
                }
                throw new BusinessRuleException("You have already asked to message this user.");
            }
            // DECLINED: reopen the caller's own row rather than inserting a
            // duplicate pair, which uk_message_request_pair forbids.
            if (current.getRequesterUserId().equals(actorUserId)) {
                messageRequestDAO.reopen(current.getRequestId(), introMessage);
                MessageRequest reopened = messageRequestDAO.findById(current.getRequestId())
                        .orElseThrow(() -> new ResourceNotFoundException("Message request was not found."));
                notifyAddressee(reopened, requester);
                return toResponse(reopened, actorUserId);
            }
            // The other side's request was declined by the caller; the caller
            // asking now is a fresh request in the opposite direction.
        }

        MessageRequest created = messageRequestDAO.create(MessageRequest.builder()
                .requesterUserId(actorUserId)
                .addresseeUserId(addresseeUserId)
                .status(MessageRequestStatus.PENDING)
                .introMessage(introMessage)
                .build());

        MessageRequest stored = messageRequestDAO.findById(created.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Message request was not found."));
        notifyAddressee(stored, requester);
        return toResponse(stored, actorUserId);
    }

    @Override
    public MessageRequestResponseDTO accept(UUID actorUserId, UUID requestId) {
        return respond(actorUserId, requestId, MessageRequestStatus.ACCEPTED);
    }

    @Override
    public MessageRequestResponseDTO decline(UUID actorUserId, UUID requestId) {
        return respond(actorUserId, requestId, MessageRequestStatus.DECLINED);
    }

    private MessageRequestResponseDTO respond(UUID actorUserId, UUID requestId, MessageRequestStatus decision) {
        User addressee = requireAuthenticated(actorUserId);
        if (requestId == null) {
            throw new ValidationException("A message request is required.");
        }

        MessageRequest request = messageRequestDAO.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Message request was not found."));

        // Only the addressee decides, and only while it is still pending —
        // without both checks a requester could accept their own request.
        if (!request.getAddresseeUserId().equals(actorUserId)) {
            throw new AuthorisationException("Only the person asked can answer a message request.");
        }
        if (request.getStatus() != MessageRequestStatus.PENDING) {
            throw new BusinessRuleException("This message request has already been answered.");
        }

        messageRequestDAO.updateStatus(requestId, decision);
        MessageRequest updated = messageRequestDAO.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Message request was not found."));

        if (decision == MessageRequestStatus.ACCEPTED) {
            notifyRequesterAccepted(updated, addressee);
        }
        return toResponse(updated, actorUserId);
    }

    @Override
    public List<MessageContactDTO> listContacts(UUID actorUserId, String searchTerm) {
        requireAuthenticated(actorUserId);

        // One pass over this user's requests, indexed by counterpart, so the
        // directory costs two queries rather than one per candidate.
        Map<UUID, MessageRequest> byCounterpart = new HashMap<>();
        for (MessageRequest request : messageRequestDAO.findAllInvolving(actorUserId)) {
            UUID counterpart = request.getRequesterUserId().equals(actorUserId)
                    ? request.getAddresseeUserId()
                    : request.getRequesterUserId();
            MessageRequest kept = byCounterpart.get(counterpart);
            if (kept == null || outranks(request, kept)) {
                byCounterpart.put(counterpart, request);
            }
        }

        List<MessageContactDTO> contacts = new ArrayList<>();
        for (User candidate : userDAO.searchUsers(searchTerm)) {
            if (candidate.getUserId().equals(actorUserId) || !Boolean.TRUE.equals(candidate.getIsActive())) {
                continue;
            }

            MessageRequest request = byCounterpart.get(candidate.getUserId());
            MessageContactState state;
            if (userBlockDAO.existsEitherDirection(actorUserId, candidate.getUserId())) {
                state = MessageContactState.BLOCKED;
            } else if (request == null) {
                state = MessageContactState.NONE;
            } else {
                state = switch (request.getStatus()) {
                    case ACCEPTED -> MessageContactState.ACCEPTED;
                    case PENDING -> request.getRequesterUserId().equals(actorUserId)
                            ? MessageContactState.REQUEST_SENT
                            : MessageContactState.REQUEST_RECEIVED;
                    case DECLINED -> MessageContactState.DECLINED;
                };
            }

            contacts.add(MessageContactDTO.builder()
                    .userId(candidate.getUserId())
                    .username(candidate.getUsername())
                    .state(state)
                    .requestId(request == null ? null : request.getRequestId())
                    .build());
        }
        return contacts;
    }

    @Override
    public boolean canExchangeMessages(UUID userA, UUID userB) {
        if (userA == null || userB == null) {
            return false;
        }
        return messageRequestDAO.isAcceptedBetween(userA, userB);
    }

    /** ACCEPTED beats PENDING beats DECLINED when both directions have a row. */
    private boolean outranks(MessageRequest candidate, MessageRequest incumbent) {
        return rank(candidate.getStatus()) < rank(incumbent.getStatus());
    }

    private int rank(MessageRequestStatus status) {
        return switch (status) {
            case ACCEPTED -> 0;
            case PENDING -> 1;
            case DECLINED -> 2;
        };
    }

    private String trimIntro(String introMessage) {
        if (introMessage == null || introMessage.isBlank()) {
            return null;
        }
        String trimmed = introMessage.trim();
        if (trimmed.length() > INTRO_MAX_LENGTH) {
            throw new ValidationException("The note may be at most " + INTRO_MAX_LENGTH + " characters.");
        }
        return trimmed;
    }

    private void notifyAddressee(MessageRequest request, User requester) {
        createNotification(request.getAddresseeUserId(),
                requester.getUsername() + " asked to message you",
                request.getRequestId());
    }

    private void notifyRequesterAccepted(MessageRequest request, User addressee) {
        createNotification(request.getRequesterUserId(),
                addressee.getUsername() + " accepted your message request",
                request.getRequestId());
    }

    // Notifications are best-effort: a failure here must not undo a request the
    // database has already accepted, matching how direct messages behave.
    private void createNotification(UUID userId, String body, UUID requestId) {
        try {
            notificationService.createNotification(NotificationCreateRequestDTO.builder()
                    .userId(userId)
                    .type(NotificationType.CHAT_MESSAGE)
                    .body(body)
                    .relatedEntityType("MESSAGE_REQUEST")
                    .relatedEntityId(requestId)
                    .build());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Unable to create in-app notification for message request", e);
        }
    }

    private MessageRequestResponseDTO toResponse(MessageRequest request, UUID actorUserId) {
        return MessageRequestResponseDTO.builder()
                .requestId(request.getRequestId())
                .requesterUserId(request.getRequesterUserId())
                .requesterUsername(request.getRequesterUsername())
                .addresseeUserId(request.getAddresseeUserId())
                .addresseeUsername(request.getAddresseeUsername())
                .status(request.getStatus())
                .introMessage(request.getIntroMessage())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .mine(request.getRequesterUserId().equals(actorUserId))
                .build();
    }

    private User activeUserOrNotFound(UUID userId) {
        User user = userDAO.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found."));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResourceNotFoundException("User was not found.");
        }
        return user;
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
