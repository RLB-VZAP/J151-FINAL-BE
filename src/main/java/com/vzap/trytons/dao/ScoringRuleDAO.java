package com.vzap.trytons.dao;

import com.vzap.trytons.model.ScoringRule;

import java.util.List;
import java.util.Optional;

public interface ScoringRuleDAO {

    List<ScoringRule> findActiveRules(String season);
    Optional<ScoringRule> findById(String ruleId);
    ScoringRule save(ScoringRule rule);
    ScoringRule update(ScoringRule rule);
}