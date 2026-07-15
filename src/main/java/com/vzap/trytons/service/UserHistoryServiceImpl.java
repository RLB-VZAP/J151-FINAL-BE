package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.FixtureDAO;
import com.vzap.trytons.dao.MatchResultDAO;
import com.vzap.trytons.dao.MatchTeamScoreDAO;
import com.vzap.trytons.dto.UserPointsHistoryResponseDTO;
import com.vzap.trytons.dto.WeeklyPerformanceResponseDTO;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.MatchResult;
import com.vzap.trytons.model.MatchTeamScore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

@ApplicationScoped
public class UserHistoryServiceImpl implements UserHistoryService {

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private MatchResultDAO matchResultDAO;

    @Inject
    private MatchTeamScoreDAO matchTeamScoreDAO;

    @Override
    public UserPointsHistoryResponseDTO getUserPointsHistory(String actorUserId) {

        UUID ownerID;
        try {
            ownerID = UUID.fromString(actorUserId);
        } catch (IllegalArgumentException e) {
            throw new AuthorisationException("No UserId found");
        }

        List<FantasyTeam> fantasyTeams = fantasyTeamDAO.findTeamsByOwner(ownerID);

        if (fantasyTeams.isEmpty()){
            throw new ResourceNotFoundException("No Fantasy Teams were found");
        }

        List<WeeklyPerformanceResponseDTO> rounds = getWeeklyPerformance(actorUserId);

        int totals = fantasyTeams.get(0).getTotalPoints();

        return UserPointsHistoryResponseDTO.builder()
                .totals(totals)
                .rounds(rounds)
                .ranking(null)
                .build();
    }

    @Override
    public List<WeeklyPerformanceResponseDTO> getWeeklyPerformance(String actorUserId) {

        UUID ownerID;
        try {
            ownerID = UUID.fromString(actorUserId);
        } catch (IllegalArgumentException e) {
            throw new AuthorisationException("No UserId found");
        }

        List<WeeklyPerformanceResponseDTO> weeklyPerformance = new ArrayList<>();

        List<FantasyTeam> fantasyTeams = fantasyTeamDAO.findTeamsByOwner(ownerID);

        for(FantasyTeam ft : fantasyTeams){
            List<Fixture> fixtures = fixtureDAO.findByTeamId(ft.getTeamId());

            UUID teamId = ft.getTeamId();

            for(Fixture fixture : fixtures){

                UUID roundId = fixture.getRoundId();

                Optional<MatchResult> resultOpt = matchResultDAO.findCurrentByFixtureId(fixture.getFixtureId());

                if (resultOpt.isEmpty()){

                    continue;

                }
                MatchResult result = resultOpt.get();

                MatchTeamSide side = MatchTeamSide.TEAM_A;

                if (!fixture.getTeamAId().equals(teamId)){
                    side = MatchTeamSide.TEAM_B;
                }

                Optional<MatchTeamScore> pointsScored = matchTeamScoreDAO.findByResultIdAndTeamSide(result.getResultId(), side);
                if (result.isDraw()){

                    if (pointsScored.isEmpty()){
                        continue;
                    } else {

                        // TODO: MatchTeamScore.score replaced by playerPoints/captainBonus/transferPenalty/totalScore — model now mirrors schema.sql
                        WeeklyPerformanceResponseDTO wpr = WeeklyPerformanceResponseDTO.builder()
                                .roundId(roundId)
                                .fixtureId(fixture.getFixtureId())
                                .pointsScored(pointsScored.get().getScore())
                                .result("DRAW")
                                .build();

                        weeklyPerformance.add(wpr);

                    }

                } else {

                    if (pointsScored.isEmpty()){
                        continue;
                    }

                    // TODO: MatchResult.winnerSide retyped String -> MatchTeamSide — model now mirrors schema.sql
                    String outcome = result.getWinnerSide();

                    if (side.name().equals(outcome)){

                        outcome = "WIN";
                    } else {
                        outcome = "LOSS";
                    }

                    // TODO: MatchTeamScore.score replaced by playerPoints/captainBonus/transferPenalty/totalScore — model now mirrors schema.sql
                    WeeklyPerformanceResponseDTO wpr = WeeklyPerformanceResponseDTO.builder()
                            .roundId(roundId)
                            .fixtureId(fixture.getFixtureId())
                            .pointsScored(pointsScored.get().getScore())
                            .result(outcome)
                            .build();

                    weeklyPerformance.add(wpr);

                }


            }

        }

        return weeklyPerformance;
    }
}
