package com.vzap.trytons.service;

import com.vzap.trytons.dto.ScoringRuleRequestDTO;
import com.vzap.trytons.dto.ScoringRuleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ScoringRuleService {

    List<ScoringRuleResponseDTO>listRules(UUID actorUserId, String season);

    ScoringRuleResponseDTO saveRule(
            UUID actorUserId,
            ScoringRuleRequestDTO request);
}
