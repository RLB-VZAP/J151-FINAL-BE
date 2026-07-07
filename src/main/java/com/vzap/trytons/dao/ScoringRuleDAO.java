package com.vzap.trytons.dao;

import com.vzap.trytons.model.ScoringRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoringRuleDAO {
    //STUB
    List<ScoringRule> findActiveRules(UUID leagueId);

    ScoringRule save(ScoringRule scoringRule);

    Optional<ScoringRule> findById(UUID ruleId);
}
