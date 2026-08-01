package com.vzap.trytons.service.results;

import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.results.MatchTeamScoreDAO;
import com.vzap.trytons.dao.results.PlayerStatisticsDAO;
import com.vzap.trytons.dto.results.PointsByEventDTO;
import com.vzap.trytons.dto.results.TeamPointsBreakdownDTO;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.results.MatchTeamScore;
import com.vzap.trytons.model.results.PointsByEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Assembles the per-team points composition shown on a fixture.
 *
 * <p>Shared by the simulation path and the result-capture path so both report
 * a score the same way.
 */
@ApplicationScoped
public class MatchPointsBreakdownServiceImpl implements MatchPointsBreakdownService {

    @Inject
    private PlayerStatisticsDAO playerStatisticsDAO;
    @Inject
    private MatchTeamScoreDAO matchTeamScoreDAO;
    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Override
    public TeamPointsBreakdownDTO breakdownFor(UUID resultId, UUID teamId) {
        if (resultId == null || teamId == null) {
            return null;
        }

        List<PointsByEvent> events =
                playerStatisticsDAO.findPointsByEventForResultAndTeam(resultId, teamId);

        List<PointsByEventDTO> eventDTOs = new ArrayList<>();
        int playerPoints = 0;
        for (PointsByEvent event : events) {
            playerPoints += event.getPointsEarned();
            eventDTOs.add(PointsByEventDTO.builder()
                    .eventType(event.getEventType())
                    .description(event.getDescription())
                    .eventCount(event.getEventCount())
                    .pointsEarned(event.getPointsEarned())
                    .deduction(event.isDeduction())
                    .build());
        }

        /*
            The stored team score is authoritative: it carries the captain
            bonus and transfer penalty, which are applied to the team rather
            than earned by any single event. Fall back to the summed event
            points when no score row exists yet.
        */
        Optional<MatchTeamScore> stored = matchTeamScoreDAO.findByResultId(resultId).stream()
                .filter(score -> teamId.equals(score.getTeamId()))
                .findFirst();

        int captainBonus = stored.map(MatchTeamScore::getCaptainBonus).orElse(0);
        int transferPenalty = stored.map(MatchTeamScore::getTransferPenalty).orElse(0);
        int storedPlayerPoints = stored.map(MatchTeamScore::getPlayerPoints).orElse(playerPoints);
        int total = stored.map(MatchTeamScore::getTotalScore)
                .orElse(playerPoints + captainBonus - transferPenalty);

        String teamName = fantasyTeamDAO.getTeamById(teamId)
                .map(FantasyTeam::getTeamName)
                .orElse(null);

        return TeamPointsBreakdownDTO.builder()
                .teamId(teamId)
                .teamName(teamName)
                .playerPoints(storedPlayerPoints)
                .captainBonus(captainBonus)
                .transferPenalty(transferPenalty)
                .totalPoints(total)
                .events(eventDTOs)
                .build();
    }
}
