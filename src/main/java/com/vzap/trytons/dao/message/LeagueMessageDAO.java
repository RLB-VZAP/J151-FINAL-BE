package com.vzap.trytons.dao.message;

import com.vzap.trytons.enums.MessageStatus;
import com.vzap.trytons.model.message.LeagueMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueMessageDAO {

    LeagueMessage create(LeagueMessage message);

    List<LeagueMessage> findApprovedByLeague(UUID leagueId, LocalDateTime since);

    List<LeagueMessage> findByStatus(MessageStatus status);

    Optional<LeagueMessage> findById(UUID messageId);

    boolean updateStatus(UUID messageId, MessageStatus status, UUID moderatorUserId);

    /**
     * True if the league has ever had a message the blocklist auto-flagged
     * (rule E: "reported" includes blocklist auto-flags, not just user reports).
     */
    boolean existsFlaggedMessage(UUID leagueId);
}
