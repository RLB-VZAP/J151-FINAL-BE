package com.vzap.trytons.scoring.service;

import com.vzap.trytons.scoring.dto.ScoringRuleRequestDTO;
import com.vzap.trytons.scoring.dto.ScoringRuleResponseDTO;

import java.util.List;
import java.util.UUID;

public class ScoringRuleServiceImpl implements ScoringRuleService {
    //STUB
    @Override
    public List<ScoringRuleResponseDTO> listRules(UUID actorUserId, UUID leagueId) {
        return List.of();
    }

    @Override
    public ScoringRuleResponseDTO saveRule(UUID actorUserId, ScoringRuleRequestDTO request) {
        return null;
    }
}
