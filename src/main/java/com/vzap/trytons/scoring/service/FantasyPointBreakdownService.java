package com.vzap.trytons.scoring.service;

import com.vzap.trytons.scoring.dto.FantasyPointBreakdownResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FantasyPointBreakdownService {
    FantasyPointBreakdownResponseDTO getBreakdownById(UUID breakdownId);
    List<FantasyPointBreakdownResponseDTO> listBreakdownsForPoints(UUID pointsId);
}