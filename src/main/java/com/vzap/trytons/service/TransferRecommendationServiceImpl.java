package com.vzap.trytons.service;

import com.vzap.trytons.dto.TransferRecommendationRequestDTO;
import com.vzap.trytons.dto.TransferRecommendationResponseDTO;

import java.util.List;
import java.util.UUID;

public class TransferRecommendationServiceImpl implements  TransferRecommendationService {
    @Override
    public List<TransferRecommendationResponseDTO> recommendTransfers(UUID actorUserId, TransferRecommendationRequestDTO request) {
        return List.of();
    }
}
