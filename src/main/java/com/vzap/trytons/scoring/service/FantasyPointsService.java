package com.vzap.trytons.scoring.service;

import com.vzap.trytons.scoring.dto.FantasyPointsRequestDTO;
import com.vzap.trytons.scoring.dto.FantasyPointsResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FantasyPointsService {
    FantasyPointsResponseDTO calculateFantasyPoints(UUID actorUserId, FantasyPointsRequestDTO request);
    FantasyPointsResponseDTO getFantasyPointsById(UUID pointsId);
    List<FantasyPointsResponseDTO> listFantasyPointsForStat(UUID statId);
    FantasyPointsResponseDTO getFinalFantasyPointsForStat(UUID statId);
}