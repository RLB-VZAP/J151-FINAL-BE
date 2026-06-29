package com.vzap.trytons.service;

import com.vzap.trytons.dto.TransferRequest;
import com.vzap.trytons.dto.TransferResponse;

import java.util.List;
import java.util.UUID;

public interface TransferService {
    TransferResponse executeTransfer(UUID requestingUserId, TransferRequest request);
    List<TransferResponse> getTransferHistoryForTeam (UUID requestingUserId, UUID teamId);
}
