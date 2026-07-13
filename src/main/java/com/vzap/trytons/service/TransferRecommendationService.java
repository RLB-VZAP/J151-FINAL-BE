package com.vzap.trytons.service;

import com.vzap.trytons.dto.TransferRecommendationRequestDTO;
import com.vzap.trytons.dto.TransferRecommendationResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransferRecommendationService {
    TransferRecommendationResponseDTO recommendTransfers(UUID actorUserId, TransferRecommendationRequestDTO request);
}
