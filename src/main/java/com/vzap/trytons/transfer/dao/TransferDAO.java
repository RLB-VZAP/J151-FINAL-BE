package com.vzap.trytons.transfer.dao;

import com.vzap.trytons.transfer.enums.TransferStatus;
import com.vzap.trytons.transfer.model.Transfer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferDAO {

    Optional<Transfer> saveTransfer(Transfer transfer);
    Optional<Transfer> getTransferById(UUID transferId);
    List<Transfer> getTransfersByTeamId(UUID teamId);
    boolean updateTransferStatus(UUID transferId, TransferStatus transferStatus, LocalDateTime confirmationDate);
    int countConfirmedTransfers(UUID teamId, UUID roundId);
    List<Transfer> getTransfersByRound(UUID roundId);
}
