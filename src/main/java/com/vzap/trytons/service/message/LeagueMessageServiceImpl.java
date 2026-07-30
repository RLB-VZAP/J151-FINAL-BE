package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dao.message.LeagueMessageDAO;
import com.vzap.trytons.dto.message.LeagueMessageResponseDTO;
import com.vzap.trytons.dto.message.PendingLeagueMessageDTO;
import com.vzap.trytons.dto.message.SendLeagueMessageRequestDTO;
import com.vzap.trytons.dto.notification.NotificationCreateRequestDTO;
import com.vzap.trytons.enums.MessageStatus;
import com.vzap.trytons.enums.NotificationType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.league.LeagueMembership;
import com.vzap.trytons.model.message.LeagueMessage;
import com.vzap.trytons.service.notification.NotificationService;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LeagueMessageServiceImpl implements LeagueMessageService {

    private static final Logger LOG = Logger.getLogger(LeagueMessageServiceImpl.class.getName());

    @Inject
    private LeagueMessageDAO leagueMessageDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private LeagueDAO leagueDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private MessageFilterService messageFilterService;
    @Inject
    private NotificationService notificationService;

    @Override
    public LeagueMessageResponseDTO post(UUID actorUserId, UUID leagueId, SendLeagueMessageRequestDTO request) {
        User sender = requireAuthenticated(actorUserId);
        if (leagueId == null) {
            throw new ValidationException("A league is required.");
        }
        String body = (request == null || request.getBody() == null) ? "" : request.getBody().trim();
        if (body.isEmpty()) {
            throw new ValidationException("Message body is required.");
        }

        League league = leagueDAO.findLeagueById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found."));
        requireMembership(leagueId, actorUserId);

        Optional<String> blockedPhrase = messageFilterService.firstBlockedPhrase(body);

        LeagueMessage message = LeagueMessage.builder()
                .leagueId(leagueId)
                .senderUserId(actorUserId)
                .body(body)
                .status(blockedPhrase.isPresent() ? MessageStatus.PENDING_REVIEW : MessageStatus.APPROVED)
                .flaggedReason(blockedPhrase.orElse(null))
                .build();
        LeagueMessage created = leagueMessageDAO.create(message);

        if (created.getStatus() == MessageStatus.APPROVED) {
            notifyMembers(league, created, actorUserId);
        }

        return mapToResponse(created, sender.getUsername());
    }

    @Override
    public List<LeagueMessageResponseDTO> getFeed(UUID actorUserId, UUID leagueId, LocalDateTime since) {
        requireAuthenticated(actorUserId);
        if (leagueId == null) {
            throw new ValidationException("A league is required.");
        }
        requireMembership(leagueId, actorUserId);

        List<LeagueMessage> messages = leagueMessageDAO.findApprovedByLeague(leagueId, since);
        Map<UUID, String> usernames = resolveUsernames(messages.stream().map(LeagueMessage::getSenderUserId).toList());

        List<LeagueMessageResponseDTO> response = new ArrayList<>();
        for (LeagueMessage message : messages) {
            response.add(mapToResponse(message, usernames.get(message.getSenderUserId())));
        }
        return response;
    }

    @Override
    public List<PendingLeagueMessageDTO> listPending() {
        List<LeagueMessage> pending = leagueMessageDAO.findByStatus(MessageStatus.PENDING_REVIEW);
        Map<UUID, String> usernames = resolveUsernames(pending.stream().map(LeagueMessage::getSenderUserId).toList());
        Map<UUID, String> leagueNames = new HashMap<>();

        List<PendingLeagueMessageDTO> response = new ArrayList<>();
        for (LeagueMessage message : pending) {
            String leagueName = leagueNames.computeIfAbsent(message.getLeagueId(),
                    id -> leagueDAO.findLeagueById(id).map(League::getLeagueName).orElse("Unknown league"));
            response.add(PendingLeagueMessageDTO.builder()
                    .messageId(message.getMessageId())
                    .leagueId(message.getLeagueId())
                    .leagueName(leagueName)
                    .senderUserId(message.getSenderUserId())
                    .senderUsername(usernames.get(message.getSenderUserId()))
                    .body(message.getBody())
                    .flaggedReason(message.getFlaggedReason())
                    .createdAt(message.getCreatedAt())
                    .build());
        }
        return response;
    }

    @Override
    public LeagueMessageResponseDTO approve(UUID adminUserId, UUID messageId) {
        LeagueMessage message = requirePending(messageId);
        leagueMessageDAO.updateStatus(messageId, MessageStatus.APPROVED, adminUserId);
        message.setStatus(MessageStatus.APPROVED);
        message.setModeratedByUserId(adminUserId);

        leagueDAO.findLeagueById(message.getLeagueId())
                .ifPresent(league -> notifyMembers(league, message, message.getSenderUserId()));

        String senderUsername = userDAO.getUserById(message.getSenderUserId())
                .map(User::getUsername).orElse(null);
        return mapToResponse(message, senderUsername);
    }

    @Override
    public void reject(UUID adminUserId, UUID messageId) {
        requirePending(messageId);
        leagueMessageDAO.updateStatus(messageId, MessageStatus.REJECTED, adminUserId);
        // Silent removal: the sender is not notified (decision #5).
    }

    private void notifyMembers(League league, LeagueMessage message, UUID senderUserId) {
        List<LeagueMembership> members = leagueMembershipDAO.findActiveByLeague(league.getLeagueId());
        for (LeagueMembership membership : members) {
            UUID memberUserId = membership.getRegisteredUserId();
            if (memberUserId == null || memberUserId.equals(senderUserId)) {
                continue;
            }
            try {
                NotificationCreateRequestDTO request = NotificationCreateRequestDTO.builder()
                        .userId(memberUserId)
                        .type(NotificationType.CHAT_MESSAGE)
                        .body("New message in " + league.getLeagueName())
                        .relatedEntityType("LEAGUE")
                        .relatedEntityId(league.getLeagueId())
                        .build();
                notificationService.createNotification(request);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Unable to notify league member of new message", e);
            }
        }
    }

    private Map<UUID, String> resolveUsernames(List<UUID> userIds) {
        Map<UUID, String> usernames = new HashMap<>();
        for (UUID userId : userIds) {
            if (userId == null || usernames.containsKey(userId)) {
                continue;
            }
            usernames.put(userId, userDAO.getUserById(userId).map(User::getUsername).orElse("Unknown user"));
        }
        return usernames;
    }

    private LeagueMessage requirePending(UUID messageId) {
        if (messageId == null) {
            throw new ValidationException("A message is required.");
        }
        LeagueMessage message = leagueMessageDAO.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found."));
        if (message.getStatus() != MessageStatus.PENDING_REVIEW) {
            throw new BusinessRuleException("This message has already been moderated.");
        }
        return message;
    }

    private void requireMembership(UUID leagueId, UUID userId) {
        if (!leagueMembershipDAO.existsActiveByLeagueAndUser(leagueId, userId)) {
            throw new AuthorisationException("You are not a member of this league.");
        }
    }

    private LeagueMessageResponseDTO mapToResponse(LeagueMessage message, String senderUsername) {
        return LeagueMessageResponseDTO.builder()
                .messageId(message.getMessageId())
                .leagueId(message.getLeagueId())
                .senderUserId(message.getSenderUserId())
                .senderUsername(senderUsername)
                .body(message.getBody())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
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
