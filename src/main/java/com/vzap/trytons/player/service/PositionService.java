package com.vzap.trytons.player.service;

import com.vzap.trytons.player.dto.PositionRequestDTO;
import com.vzap.trytons.player.dto.PositionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PositionService {
    PositionResponseDTO createPosition(PositionRequestDTO request);
    PositionResponseDTO getPosition(UUID positionId);
    List<PositionResponseDTO> getAllPositions();
    PositionResponseDTO updatePosition(UUID positionId, PositionRequestDTO request);
}