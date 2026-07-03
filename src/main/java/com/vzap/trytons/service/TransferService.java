package com.vzap.trytons.service;

import com.vzap.trytons.dto.TransferRequestDTO;
import com.vzap.trytons.dto.TransferResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransferService {
    TransferResponseDTO executeTransfer(UUID requestingUserId, TransferRequestDTO request);
    List<TransferResponseDTO> getTransferHistoryForTeam (UUID requestingUserId, UUID teamId);
}
