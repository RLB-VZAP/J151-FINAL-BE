package com.vzap.trytons.service;

import com.vzap.trytons.dto.PositionRequestDTO;
import com.vzap.trytons.dto.PositionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PositionService {
    PositionResponseDTO createPostion(PositionResponseDTO request);
    PositionResponseDTO getPosition(UUID positionId);
    List<PositionResponseDTO> getAllPositons();
    PositionResponseDTO updatePlayer(UUID playerId, PositionRequestDTO request);
}
