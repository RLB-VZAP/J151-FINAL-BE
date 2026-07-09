package com.vzap.trytons.transfer.service;

import com.vzap.trytons.transfer.dto.TransferRecommendationRequestDTO;
import com.vzap.trytons.transfer.dto.TransferRecommendationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransferRecommendationService {
    List<TransferRecommendationResponseDTO> recommendTransfers(
            UUID actorUserId, TransferRecommendationRequestDTO request);
}
