package com.vzap.trytons.service;

import com.vzap.trytons.dao.*;
import com.vzap.trytons.dto.TeamScoreUpdateResultDTO;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Inject FantasyTeamDAO fantasyTeamDAO;

    @Inject
    FantasyRoundDAO fantasyRoundDAO;

    @Override
    public TeamScoreUpdateResultDTO updateTeamScoresForFixture(String fixtureId) {

        Fixture currentFixture = fixtureDAO.findFixtureById(UUID.fromString(fixtureId)).orElseThrow(() -> new ResourceNotFoundException("Unable to find fixture"));

        MatchResult currentMatchResult = matchResultDAO.findCurrentByFixtureId(currentFixture.getFixtureId()).orElseThrow(() -> new ResourceNotFoundException("Unable to find match result"));

        UUID resultId = currentMatchResult.getResultId();

        UUID currentRoundId = currentFixture.getRoundId();

        //get round squad for Team A and Team B

        UUID teamAId = currentFixture.getTeamAId();

        UUID teamBId = currentFixture.getTeamBId();

        List<FantasyTeamRoundSelection> teamARoundSelection = fantasyTeamRoundSelectionDAO.getSelectionsByRoundIdAndTeamId(currentRoundId, teamAId);

        List<FantasyTeamRoundSelection> teamBRoundSelection = fantasyTeamRoundSelectionDAO.getSelectionsByRoundIdAndTeamId(currentRoundId, teamBId);

        //loop through team A team to find each player and their fixture statistics and final FantasyPoints

        int teamATotal = 0;

        for (FantasyTeamRoundSelection selection : teamARoundSelection) {

            UUID playerId = selection.getPlayerId();

            Optional<PlayerStatistics> statisticsOpt = playerStatisticsDAO.findByResultIdAndTeamIdAndPlayerId(resultId, teamAId, playerId);

            if (statisticsOpt.isEmpty()) {
                continue;
            }

            Optional<FantasyPoints> finalPointsOpt = fantasyPointsDAO.findFinalByStatId(statisticsOpt.get().getStatId());

            if (finalPointsOpt.isEmpty()) {

                throw new BusinessRuleException("Points have not been calculated for this fixture yet");
            }

            teamATotal += finalPointsOpt.get().getTotalPoints();

        }

        //do the same for team B
        int teamBTotal = 0;

        for (FantasyTeamRoundSelection selection : teamBRoundSelection) {

            UUID playerId = selection.getPlayerId();

            Optional<PlayerStatistics> statisticsOpt = playerStatisticsDAO.findByResultIdAndTeamIdAndPlayerId(resultId, teamBId, playerId);

            if (statisticsOpt.isEmpty()) {
                continue;
            }

            Optional<FantasyPoints> finalPointsOpt = fantasyPointsDAO.findFinalByStatId(statisticsOpt.get().getStatId());

            if (finalPointsOpt.isEmpty()) {

                throw new BusinessRuleException("Points have not been calculated for this fixture yet");

            }

            teamBTotal += finalPointsOpt.get().getTotalPoints();
        }

        //find team scores

        Optional<MatchTeamScore> existingTeamAScore = matchTeamScoreDAO.findByResultIdAndTeamSide(resultId, MatchTeamSide.TEAM_A);

        MatchTeamScore teamAScore;
        if (existingTeamAScore.isPresent()) {
            MatchTeamScore scoreToUpdate = existingTeamAScore.get();
            scoreToUpdate.setPlayerPoints(teamATotal);
            scoreToUpdate.setCaptainBonus(0);
            scoreToUpdate.setTransferPenalty(0);
            scoreToUpdate.setCalculatedAt(LocalDateTime.now());
            teamAScore = matchTeamScoreDAO.update(scoreToUpdate);
        } else {
            MatchTeamScore scoreToCreate = MatchTeamScore.builder()
                    .resultId(resultId)
                    .teamId(teamAId)
                    .teamSide(MatchTeamSide.TEAM_A)
                    .playerPoints(teamATotal)
                    .captainBonus(0)
                    .transferPenalty(0)
                    .calculatedAt(LocalDateTime.now())
                    .build();
            teamAScore = matchTeamScoreDAO.save(scoreToCreate);
        }

        Optional<MatchTeamScore> existingTeamBScore = matchTeamScoreDAO.findByResultIdAndTeamSide(resultId, MatchTeamSide.TEAM_B);

        MatchTeamScore teamBScore;
        if (existingTeamBScore.isPresent()) {
            MatchTeamScore scoreToUpdate = existingTeamBScore.get();
            scoreToUpdate.setPlayerPoints(teamBTotal);
            scoreToUpdate.setCaptainBonus(0);
            scoreToUpdate.setTransferPenalty(0);
            scoreToUpdate.setCalculatedAt(LocalDateTime.now());
            teamBScore = matchTeamScoreDAO.update(scoreToUpdate);
        } else {
            MatchTeamScore scoreToCreate = MatchTeamScore.builder()
                    .resultId(resultId)
                    .teamId(teamBId)
                    .teamSide(MatchTeamSide.TEAM_B)
                    .playerPoints(teamBTotal)
                    .captainBonus(0)
                    .transferPenalty(0)
                    .calculatedAt(LocalDateTime.now())
                    .build();
            teamBScore = matchTeamScoreDAO.save(scoreToCreate);
        }

        //find outcome of match

            String outcome;

            if (teamAScore.getTotalScore() > teamBScore.getTotalScore()) {
                outcome = "TEAM_A";
            } else if (teamAScore.getTotalScore() < teamBScore.getTotalScore()) {
                outcome = "TEAM_B";
            } else {
                outcome = "DRAW";
            }


            UUID currentLeagueId = currentFixture.getLeagueId();

            leaderboardService.refreshLeagueLeaderboard(null, currentLeagueId);

            //return the updated result

        return TeamScoreUpdateResultDTO.builder()
                .fixtureId(fixtureId)
                .teamATotal(teamATotal)
                .teamBTotal(teamBTotal)
                .outcome(outcome)
                .build();
        }


    @Override
    public TeamScoreUpdateResultDTO recalculateTeamTotals(String teamId, String season) {

        UUID currentTeamId = UUID.fromString(teamId);

        FantasyTeam currentFantasyTeam = fantasyTeamDAO.findTeamById(currentTeamId);

        if (currentFantasyTeam == null){
            throw new ResourceNotFoundException("no fantasy team could be found");
        }

        List<Fixture> allFixtures = fixtureDAO.findByTeamId(currentTeamId);

        List<Fixture> currentSeasonFixtures = new ArrayList<>();

        for (Fixture fixture : allFixtures){

            FantasyRound currentRound = fantasyRoundDAO.getRoundById(fixture.getRoundId()).orElseThrow(() -> new ResourceNotFoundException("no round found"));

            if (currentRound.getSeason().equals(season)){
                currentSeasonFixtures.add(fixture);
            }
        }



        MatchTeamSide team;
        int seasonTotal = 0;

        for (Fixture fixture : currentSeasonFixtures){

            MatchResult result = matchResultDAO.findCurrentByFixtureId(fixture.getFixtureId()).orElseThrow(() -> new ResourceNotFoundException("could not find result"));

            UUID resultUuid = result.getResultId();

            if (fixture.getTeamAId().equals(currentTeamId)){
                team = MatchTeamSide.TEAM_A;
            } else {
                team = MatchTeamSide.TEAM_B;
            }

            Optional<MatchTeamScore> teamScore = matchTeamScoreDAO.findByResultIdAndTeamSide(resultUuid, team);

            if (teamScore.isEmpty()){
                continue;
            }

            seasonTotal += teamScore.get().getTotalScore();

        }

        return TeamScoreUpdateResultDTO.builder()
                .teamId(teamId)
                .seasonTotal(seasonTotal)
                .build();
    }
}

