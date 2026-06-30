package com.vzap.trytons.dao;

import com.vzap.trytons.model.Transfer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferDAO {

    Optional<Transfer> saveTransfer(Transfer transfer);
    Optional<Transfer> getTransferById(UUID transferId);
    List<Transfer> getTransfersByTeamId(UUID teamId);
}
