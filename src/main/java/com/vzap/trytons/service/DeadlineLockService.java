package com.vzap.trytons.service;

import com.vzap.trytons.dto.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.LockStatusResponseDTO;
import com.vzap.trytons.model.Player;

import java.util.List;
import java.util.UUID;

public interface DeadlineLockService {
    LockStatusResponseDTO getLockStatus(UUID roundId);
    DeadlineStatusResponseDTO getDeadlineStatus(UUID roundId);
    LockStatusResponseDTO lockRound(UUID actorAdminUserId, UUID roundId, String reason);
    List<UUID> getLockedTeamIds(UUID roundId);
    List<UUID> getLockedPlayerIds(UUID roundId, UUID teamId);
    List<Player> getAvailableTransferPlayers(UUID roundId, UUID teamId);
}