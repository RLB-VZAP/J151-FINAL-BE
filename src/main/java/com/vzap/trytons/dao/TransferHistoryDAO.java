package com.vzap.trytons.dao;

import com.vzap.trytons.model.TransferHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferHistoryDAO {
    Optional<TransferHistory> saveTransferHistory(TransferHistory transferHistory);
    List<TransferHistory> getHistoryByTeamId(UUID teamId);
}
