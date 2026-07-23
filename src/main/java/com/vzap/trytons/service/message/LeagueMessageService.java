package com.vzap.trytons.service.message;

import com.vzap.trytons.dto.message.LeagueMessageResponseDTO;
import com.vzap.trytons.dto.message.PendingLeagueMessageDTO;
import com.vzap.trytons.dto.message.SendLeagueMessageRequestDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LeagueMessageService {

    LeagueMessageResponseDTO post(UUID actorUserId, UUID leagueId, SendLeagueMessageRequestDTO request);

    List<LeagueMessageResponseDTO> getFeed(UUID actorUserId, UUID leagueId, LocalDateTime since);

    List<PendingLeagueMessageDTO> listPending();

    LeagueMessageResponseDTO approve(UUID adminUserId, UUID messageId);

    void reject(UUID adminUserId, UUID messageId);
}
