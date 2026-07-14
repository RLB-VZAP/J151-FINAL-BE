package com.vzap.trytons.service;

import com.vzap.trytons.dao.*;
import com.vzap.trytons.dto.TeamScoreUpdateResultDTO;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TeamScoreServiceImpl implements TeamScoreService {

    @Inject
    MatchTeamScoreDAO matchTeamScoreDAO;

    @Inject
    LeaderboardService leaderboardService;

    @Inject
    FantasyTeamRoundSelectionDAO fantasyTeamRoundSelectionDAO;

    @Inject
    FixtureDAO fixtureDAO;

    @Inject
    MatchResultDAO matchResultDAO;

    @Inject
    PlayerStatisticsDAO playerStatisticsDAO;

    @Inject
    FantasyPointsDAO fantasyPointsDAO;

    @Override
    public TeamScoreUpdateResultDTO updateTeamScoresForFixture(String fixtureId) {

        Fixture currentFixture = fixtureDAO.findFixtureById(UUID.fromString(fixtureId)).orElseThrow(() -> new ResourceNotFoundException("Unable to find fixture"));

        MatchResult currentMatchResult = matchResultDAO.findCurrentByFixtureId(currentFixture.getFixtureId()).orElseThrow(() -> new ResourceNotFoundException("Unable to find match result"));

        UUID resultId = currentMatchResult.getResultId();

        UUID currentRoundId = currentFixture.getRoundId().getRoundId();


        //get round squad for Team A and Team B

        UUID teamAId = currentFixture.getTeamA().getTeamId();

        UUID teamBId = currentFixture.getTeamB().getTeamId();

        List<FantasyTeamRoundSelection> teamARoundSelection = fantasyTeamRoundSelectionDAO.getSelectionsByRoundIdAndTeamId(currentRoundId, teamAId);

        List<FantasyTeamRoundSelection> teamBRoundSelection = fantasyTeamRoundSelectionDAO.getSelectionsByRoundIdAndTeamId(currentRoundId, teamBId);

        //loop through team A team to find each player and their fixture statistics and final FantasyPoints

        int teamATotal = 0;

        for (FantasyTeamRoundSelection selection : teamARoundSelection){

            UUID playerId = selection.getPlayerId();

            Optional<PlayerStatistics> statisticsOpt = playerStatisticsDAO.findByResultIdAndTeamIdAndPlayerId(resultId, teamAId, playerId);

            if (statisticsOpt.isEmpty()){
                continue;
            }

            Optional<FantasyPoints> finalPointsOpt = fantasyPointsDAO.findFinalByStatId(statisticsOpt.get().getStatId());

            if (finalPointsOpt.isEmpty()){

                throw new BusinessRuleException("Points have not been calculated for this fixture yet");
            }

            teamATotal += finalPointsOpt.get().getTotalPoints();

        }

        //do the same for team B
        int teamBTotal = 0;

        for (FantasyTeamRoundSelection selection : teamBRoundSelection){

            UUID playerId = selection.getPlayerId();

            Optional<PlayerStatistics> statisticsOpt = playerStatisticsDAO.findByResultIdAndTeamIdAndPlayerId(resultId, teamBId, playerId);

            if (statisticsOpt.isEmpty()){
                continue;
            }

            Optional<FantasyPoints> finalPointsOpt = fantasyPointsDAO.findFinalByStatId(statisticsOpt.get().getStatId());

            if (finalPointsOpt.isEmpty()){

                throw new BusinessRuleException("Points have not been calculated for this fixture yet");

            }

            teamBTotal += finalPointsOpt.get().getTotalPoints();
        }





        return null;
    }

    @Override
    public TeamScoreUpdateResultDTO recalculateTeamTotals(String teamId, String season) {
        return null;
    }
}
