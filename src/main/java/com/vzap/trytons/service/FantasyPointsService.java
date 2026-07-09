package com.vzap.trytons.service;

import com.vzap.trytons.dto.FantasyPointsRequestDTO;
import com.vzap.trytons.dto.FantasyPointsResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FantasyPointsService {
    FantasyPointsResponseDTO calculateFantasyPoints(UUID actorUserId, FantasyPointsRequestDTO request);
    FantasyPointsResponseDTO getFantasyPointsById(UUID pointsId);
    List<FantasyPointsResponseDTO> listFantasyPointsForStat(UUID statId);
    FantasyPointsResponseDTO getFinalFantasyPointsForStat(UUID statId);
}