package com.vzap.trytons.simulation.service;

import com.vzap.trytons.simulation.dto.DeadlineStatusResponseDTO;
import com.vzap.trytons.simulation.dto.LockStatusResponseDTO;
import com.vzap.trytons.player.model.Player;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DeadlineLockServiceImpl implements DeadlineLockService {

    @Override
    public LockStatusResponseDTO getLockStatus(UUID roundId) {
        throw new UnsupportedOperationException(
                "DeadlineLockServiceImpl is a stub. Full implementation belongs to W3-BE-DATABASE-LOGIC-FIX-04A."
        );
    }

    @Override
    public DeadlineStatusResponseDTO getDeadlineStatus(UUID roundId) {
        throw new UnsupportedOperationException(
                "DeadlineLockServiceImpl is a stub. Full implementation belongs to W3-BE-DATABASE-LOGIC-FIX-04A."
        );
    }

    @Override
    public LockStatusResponseDTO lockRound(UUID actorAdminUserId, UUID roundId, String reason) {
        throw new UnsupportedOperationException(
                "DeadlineLockServiceImpl is a stub. Full implementation belongs to W3-BE-DATABASE-LOGIC-FIX-04A."
        );
    }

    @Override
    public List<UUID> getLockedTeamIds(UUID roundId) {
        throw new UnsupportedOperationException(
                "DeadlineLockServiceImpl is a stub. Full implementation belongs to W3-BE-DATABASE-LOGIC-FIX-04A."
        );
    }

    @Override
    public List<UUID> getLockedPlayerIds(UUID roundId, UUID teamId) {
        throw new UnsupportedOperationException(
                "DeadlineLockServiceImpl is a stub. Full implementation belongs to W3-BE-DATABASE-LOGIC-FIX-04A."
        );
    }

    @Override
    public List<Player> getAvailableTransferPlayers(UUID roundId, UUID teamId) {
        throw new UnsupportedOperationException(
                "DeadlineLockServiceImpl is a stub. Full implementation belongs to W3-BE-DATABASE-LOGIC-FIX-04A."
        );
    }
}