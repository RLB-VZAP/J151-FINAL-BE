package com.vzap.trytons.transfer.service;

import com.vzap.trytons.transfer.dto.TransferRequestDTO;
import com.vzap.trytons.transfer.dto.TransferResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransferService {
    TransferResponseDTO executeTransfer(UUID requestingUserId, TransferRequestDTO request);
    List<TransferResponseDTO> getTransfersForTeam(UUID requestingUserId, UUID teamId);
}
