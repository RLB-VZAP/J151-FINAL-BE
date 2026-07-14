package com.vzap.trytons.service;

import com.vzap.trytons.dao.FixtureDAO;
import com.vzap.trytons.dao.PlayerStatisticsDAO;
import com.vzap.trytons.dao.ScoringRuleDAO;
import com.vzap.trytons.dto.FantasyPointCalculationResultDTO;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.PlayerStatistics;
import com.vzap.trytons.model.ScoringRule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class FantasyPointCalculationServiceImpl implements FantasyPointCalculationService {

    @Inject
    FixtureDAO fixtureDAO;

    @Inject
    PlayerStatisticsDAO playerStatisticsDAO;

    @Inject
    ScoringRuleDAO scoringRuleDAO;

    @Override
    public FantasyPointCalculationResultDTO calculateForFixture(String fixtureId) {
        UUID currentFixtureId = UUID.fromString(fixtureId);

        Fixture currentFixture = fixtureDAO.findById(currentFixtureId).orElseThrow(() -> new ResourceNotFoundException("Fixture was not found"));
        UUID leagueId = currentFixture.getLeagueId().getLeagueId();

        List<PlayerStatistics> playerStatistics = playerStatisticsDAO.findByFixtureId(currentFixtureId);

        List<ScoringRule> scoringRules = scoringRuleDAO.findActiveRules(leagueId);

        if (scoringRules.isEmpty()){
            throw new BusinessRuleException("no scoring rules were found");
        }

        interface EventCountLookup {
            int countFor(PlayerStatistics stats);
        }

        Map<String, EventCountLookup> countLookups = Map.of(
                "TRY", PlayerStatistics::getTries,
                "CONVERSION", PlayerStatistics::getConversions,
                "PENALTY", PlayerStatistics::getPenalties,
                "ASSIST", PlayerStatistics::getAssists,
                "METERS_GAINED", PlayerStatistics::getMetersGained,
                "TACKLE", PlayerStatistics::getTackles,
                "RED_CARD", PlayerStatistics::getRedCards,
                "YELLOW_CARD", PlayerStatistics::getYellowCards
        );

        int total = 0;
        for (ScoringRule rule : scoringRules) {
            EventCountLookup lookup = countLookups.get(rule.getEventType());
            if (lookup == null) continue;

            int eventCount = lookup.countFor(statistic);
            if (eventCount > 0) {
                int contribution = eventCount * rule.getPointsAwarded();
                if (rule.isDeduction()) contribution = -contribution;
                total += contribution;
            }
        }


    }
}
