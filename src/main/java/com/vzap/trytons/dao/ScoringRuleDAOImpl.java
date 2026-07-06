package com.vzap.trytons.dao;

import com.vzap.trytons.model.ScoringRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ScoringRuleDAOImpl extends BaseDAO implements ScoringRuleDAO {
    //STUB
    @Override
    public List<ScoringRule> findActiveRules(UUID leagueId) {
        return List.of();
    }

    @Override
    public ScoringRule save(ScoringRule scoringRule) {
        return null;
    }

    @Override
    public Optional<ScoringRule> findById(UUID ruleId) {
        return Optional.empty();
    }


}
