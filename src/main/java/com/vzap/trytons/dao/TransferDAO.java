package com.vzap.trytons.dao;

import com.vzap.trytons.model.Transfer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferDAO {

    Optional<Transfer> saveTransfer(Transfer transfer);
    Optional<Transfer> getTransferById(UUID transferId);
    List<Transfer> getTransfersByTeamId(UUID teamId);

    List<Transfer> findHistoryForTeam(UUID teamId);
    int countTransfersForTeamInRound(UUID teamId, int roundNumber);
    boolean existsDuplicateTransfer(UUID teamId, UUID removedPlayerId, UUID addedPlayerId, int roundNumber);
    boolean existsPlayerConflict(UUID teamId, UUID playerId, int roundNumber);
}
