package com.vzap.trytons.service;

import com.vzap.trytons.dto.TransferRequestDTO;
import com.vzap.trytons.dto.TransferResponseDTO;

import java.util.List;

public interface TransferService {

    TransferResponseDTO executeTransfer(String actorUserId, TransferRequestDTO request);
    List<TransferResponseDTO> listTransferHistory(String actorUserId, String teamId);
}