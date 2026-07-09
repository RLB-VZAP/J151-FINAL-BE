package com.vzap.trytons.scoring.dao;

import com.vzap.trytons.scoring.model.ScoringRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoringRuleDAO {
    //STUB
    List<ScoringRule> findActiveRules(UUID leagueId);

    ScoringRule save(ScoringRule scoringRule);

    Optional<ScoringRule> findById(UUID ruleId);
}
