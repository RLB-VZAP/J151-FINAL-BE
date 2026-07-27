package com.vzap.trytons.service.message;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dao.message.DirectMessageDAO;
import com.vzap.trytons.dao.message.LeagueMessageDAO;
import com.vzap.trytons.dao.message.MessageReportDAO;
import com.vzap.trytons.dto.message.MessageReportResponseDTO;
import com.vzap.trytons.enums.MessageScope;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.message.DirectMessage;
import com.vzap.trytons.model.message.LeagueMessage;
import com.vzap.trytons.model.message.MessageReport;
import jakarta.inject.Inject;

import java.util.UUID;

public class MessageReportServiceImpl implements MessageReportService {

    @Inject
    private MessageReportDAO messageReportDAO;
    @Inject
    private DirectMessageDAO directMessageDAO;
    @Inject
    private LeagueMessageDAO leagueMessageDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public MessageReportResponseDTO report(UUID actorUserId, MessageScope scope, UUID messageId, String reason) {
        requireAuthenticated(actorUserId);
        if (scope == null || messageId == null) {
            throw new ValidationException("A message and its scope are required.");
        }

        if (scope == MessageScope.DIRECT) {
            requireDirectMessageParticipant(actorUserId, messageId);
        } else {
            requireLeagueMember(actorUserId, messageId);
        }

        MessageReport created = messageReportDAO.create(MessageReport.builder()
                .reporterUserId(actorUserId)
                .messageId(messageId)
                .messageScope(scope)
                .reason(reason)
                .build());

        return mapToResponse(created);
    }

    private void requireDirectMessageParticipant(UUID actorUserId, UUID messageId) {
        DirectMessage message = directMessageDAO.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found."));
        boolean isParticipant = actorUserId.equals(message.getSenderUserId())
                || actorUserId.equals(message.getRecipientUserId());
        if (!isParticipant) {
            throw new AuthorisationException("You may only report messages in your own conversations.");
        }
    }

    private void requireLeagueMember(UUID actorUserId, UUID messageId) {
        LeagueMessage message = leagueMessageDAO.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found."));
        if (!leagueMembershipDAO.existsActiveByLeagueAndUser(message.getLeagueId(), actorUserId)) {
            throw new AuthorisationException("You may only report messages in leagues you belong to.");
        }
    }

    private MessageReportResponseDTO mapToResponse(MessageReport report) {
        return MessageReportResponseDTO.builder()
                .reportId(report.getReportId())
                .reporterUserId(report.getReporterUserId())
                .messageId(report.getMessageId())
                .messageScope(report.getMessageScope())
                .reason(report.getReason())
                .createdAt(report.getCreatedAt())
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
