package com.vzap.trytons.service;

import com.vzap.trytons.dao.AdministratorDAO;
import com.vzap.trytons.dao.FixtureDAO;
import com.vzap.trytons.dto.LeaderboardRefreshResultDTO;
import com.vzap.trytons.dto.MatchProcessingResultDTO;
import com.vzap.trytons.dto.MatchResultResponseDTO;
import com.vzap.trytons.dto.PlayerStatisticsResponseDTO;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.Fixture;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MatchProcessingServiceImpl implements MatchProcessingService {

    private static final Logger LOG = Logger.getLogger(MatchProcessingServiceImpl.class.getName());

    // A fixture is a single match between two teams, so a successful run always
    // refreshes both sides' match_team_score rows.
    private static final int TEAMS_PER_FIXTURE = 2;

    @Inject
    private AdministratorDAO administratorDAO;
    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private MatchResultService matchResultService;
    @Inject
    private PlayerStatisticsService playerStatisticsService;
    @Inject
    private FantasyPointCalculationService fantasyPointCalculationService;
    @Inject
    private TeamScoreService teamScoreService;
    @Inject
    private LeaderboardService leaderboardService;

    @Override
    public MatchProcessingResultDTO processCompletedFixture(UUID actorUserId, UUID fixtureId) {
        if (actorUserId == null || fixtureId == null) {
            throw new BusinessRuleException("Actor and fixture identifiers are required to process a fixture.");
        }
        requireAdmin(actorUserId);
        Fixture fixture = fixtureDAO.findFixtureById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture " + fixtureId + " was not found."));
        if (fixture.getStatus() == FixtureStatus.PROCESSED) {
            throw new ConflictException("Fixture " + fixtureId + " has already been processed.");
        }
        if (fixture.getStatus() != FixtureStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Fixture " + fixtureId + " is not in a processable state (current status: " + fixture.getStatus() + ").");
        }
        MatchResultResponseDTO currentResult = matchResultService.getResult(fixtureId);
        if (currentResult == null) {
            throw new BusinessRuleException("Fixture " + fixtureId + " has no current result to process.");
        }
        List<PlayerStatisticsResponseDTO> statistics =
                playerStatisticsService.listResultStatistics(currentResult.getResultId());
        if (statistics == null || statistics.isEmpty()) {
            throw new BusinessRuleException("Fixture " + fixtureId + " has no player statistics captured for its result.");
        }
        fantasyPointCalculationService.calculateForFixture(actorUserId, fixtureId);
        teamScoreService.refreshTeamScores(actorUserId, fixtureId);
        boolean leaderboardsRefreshed = refreshLeaderboards(actorUserId, fixture);
        fixture.setStatus(FixtureStatus.PROCESSED);
        fixtureDAO.updateFixture(fixture);

        LOG.log(Level.INFO, "Processed fixture {0}: {1} statistics scored, {2} team scores refreshed.",
                new Object[]{fixtureId, statistics.size(), TEAMS_PER_FIXTURE});

        return MatchProcessingResultDTO.builder()
                .fixtureId(fixtureId)
                .pointsCalculated(statistics.size())
                .teamsUpdated(TEAMS_PER_FIXTURE)
                .leaderboardsRefreshed(leaderboardsRefreshed)
                .status(fixture.getStatus().name())
                .build();
    }

    private void requireAdmin(UUID actorUserId) {
        if (administratorDAO.getAdministratorById(actorUserId).isEmpty()) {
            throw new AuthorisationException("Only administrators may process match results.");
        }
    }

    private boolean refreshLeaderboards(UUID actorUserId, Fixture fixture) {
        if (fixture.getLeague() == null) {
            return false;
        }
        LeaderboardRefreshResultDTO refresh =
                leaderboardService.refreshLeagueLeaderboard(actorUserId, fixture.getLeague().getLeagueId());
        return refresh != null && refresh.isSuccess();
    }
}
