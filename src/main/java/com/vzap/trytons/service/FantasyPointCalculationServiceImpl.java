package com.vzap.trytons.service;

import com.vzap.trytons.dao.*;
import com.vzap.trytons.dto.FantasyPointCalculationResultDTO;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Inject
    MatchResultDAO matchResultDAO;

    @Inject
    FantasyPointsDAO fantasyPointsDAO;

    @Inject
    FantasyPointBreakdownDAO fantasyPointBreakdownDAO;

    @Override
    public FantasyPointCalculationResultDTO calculateForFixture(String fixtureId) {
        UUID currentFixtureId = UUID.fromString(fixtureId);

        MatchResult currentResult = matchResultDAO.findCurrentByFixtureId(currentFixtureId).orElseThrow(() -> new ResourceNotFoundException("No current result exists for this fixture"));

        List<PlayerStatistics> playerStatistics = playerStatisticsDAO.findByResultId(currentResult.getResultId());

        if (playerStatistics.isEmpty()){
            throw new BusinessRuleException("no player statistics were found");
        }

        Fixture currentFixture = fixtureDAO.findById(currentFixtureId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture was not found"));
        UUID leagueId = currentFixture.getLeagueId().getLeagueId();

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
        int pointsRowsWritten = 0;
        int finalCalculationVersion = 1;

        for (PlayerStatistics statistic : playerStatistics) {
            total = 0;
            List<FantasyPointBreakdown> pointBreakdowns = new ArrayList<>();

            for (ScoringRule rule : scoringRules) {
                EventCountLookup lookup = countLookups.get(rule.getEventType());
                if (lookup == null) continue;

                int eventCount = lookup.countFor(statistic);
                if (eventCount > 0) {
                    int contribution = eventCount * rule.getPointsAwarded();
                    if (rule.getIsDeduction()) contribution = -contribution;
                    total += contribution;

                    pointBreakdowns.add(FantasyPointBreakdown.builder()
                            .ruleId(rule.getRuleId())
                            .eventCount(eventCount)
                            .pointsEarned(contribution)
                            .build());

                }
            }

                UUID statId = statistic.getStatId();

                int nextVersion = fantasyPointsDAO.getNextCalculationVersion(statId);

                fantasyPointsDAO.markExistingPointsForStatAsNotFinal(statId);

                FantasyPoints savedFantasyPoints = fantasyPointsDAO.save(
                        FantasyPoints.builder()
                                .statId(statId)
                                .totalPoints(total)
                                .calculationVersion(nextVersion)
                                .finalVersion(true)
                                .calculationDate(LocalDateTime.now())
                                .build()
                );

            for (FantasyPointBreakdown breakdown : pointBreakdowns) {
                breakdown.setPointsId(savedFantasyPoints.getPointsId());
                fantasyPointBreakdownDAO.save(breakdown);
            }

            pointsRowsWritten++;
            finalCalculationVersion = savedFantasyPoints.getCalculationVersion();
            }

        FantasyPointCalculationResultDTO fantasyPointCalculationResultDTO = FantasyPointCalculationResultDTO.builder()
                .fixtureId(currentFixtureId.toString())
                .pointsRowsWritten(pointsRowsWritten)
                .calculationVersion(finalCalculationVersion)
                .build();

        return fantasyPointCalculationResultDTO;

    }
}
