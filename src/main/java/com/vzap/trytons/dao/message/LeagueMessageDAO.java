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
}
