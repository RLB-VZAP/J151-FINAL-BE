package com.vzap.trytons.service;

import com.vzap.trytons.dto.DeadlineStatusResponseDTO;
import com.vzap.trytons.dto.LockStatusResponseDTO;
import com.vzap.trytons.model.Player;

import java.util.List;
import java.util.UUID;

public interface DeadlineLockService {
    //STUB
    public LockStatusResponseDTO getLockStatus(UUID fixtureId);
    public DeadlineStatusResponseDTO getDeadlineStatus(UUID fixtureId);
    public List<UUID> getLockedTeamIds(UUID fixtureId);
    public List<UUID> getLockedPlayerIds(UUID fixtureId);
    public List<Player> getAvailableTransferPlayers(UUID fixtureId);
}
