package com.vzap.trytons.scoring.service;

import com.vzap.trytons.scoring.dto.ScoringRuleRequestDTO;
import com.vzap.trytons.scoring.dto.ScoringRuleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ScoringRuleService {
    //STUB
    List<ScoringRuleResponseDTO> listRules(
            UUID actorUserId,
            UUID leagueId);

    ScoringRuleResponseDTO saveRule(
            UUID actorUserId,
            ScoringRuleRequestDTO request);
}
