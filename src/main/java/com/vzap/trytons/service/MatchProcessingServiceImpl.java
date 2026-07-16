package com.vzap.trytons.service;

import com.vzap.trytons.dao.AdminDAO;
import com.vzap.trytons.dao.FixtureDAO;
import com.vzap.trytons.dao.MatchTeamScoreDAO;
import com.vzap.trytons.dto.LeaderboardRefreshResultDTO;
import com.vzap.trytons.dto.MatchProcessingResultDTO;
import com.vzap.trytons.dto.MatchResultResponseDTO;
import com.vzap.trytons.dto.PlayerStatisticsResponseDTO;
import com.vzap.trytons.dto.TeamScoreUpdateResultDTO;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.MatchTeamScore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MatchProcessingServiceImpl implements MatchProcessingService {

    private static final Logger LOG = Logger.getLogger(MatchProcessingServiceImpl.class.getName());

    private static final int TEAMS_PER_FIXTURE = 2;

    @Inject
    private AdminDAO adminDAO;

    @Inject
    private FixtureDAO fixtureDAO;

    @Inject
    private MatchTeamScoreDAO matchTeamScoreDAO;

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
        if (actorUserId == null) {
            throw new ValidationException("Actor user ID is required.");
        }

        if (fixtureId == null) {
            throw new ValidationException("Fixture ID is required.");
        }

        requireAdmin(actorUserId);

        Fixture fixture = fixtureDAO.findById(fixtureId).orElseThrow(() -> new ResourceNotFoundException("Fixture not found."));

        if (fixture.getStatus() == FixtureStatus.PROCESSED) {
            throw new ConflictException("The fixture has already been processed.");
        }

        if (fixture.getStatus() != FixtureStatus.COMPLETED) {
            throw new BusinessRuleException("The fixture is not completed.");
        }

        MatchResultResponseDTO currentResult = matchResultService.getResult(fixtureId);
        if (currentResult == null) {
            throw new BusinessRuleException("The fixture has no current result.");
        }

        List<PlayerStatisticsResponseDTO> statistics = playerStatisticsService.listResultStatistics(currentResult.getResultId());
        if (statistics.isEmpty()) {
            throw new BusinessRuleException("The fixture has no player statistics.");
        }

        fantasyPointCalculationService.calculateForFixture(fixtureId.toString());

        TeamScoreUpdateResultDTO scoreUpdate = teamScoreService.updateTeamScoresForFixture(fixtureId.toString());
        if (scoreUpdate == null) {
            throw new BusinessRuleException("Team score update did not complete.");
        }

        List<MatchTeamScore> persistedScores = matchTeamScoreDAO.findByResultId(currentResult.getResultId());
        int teamsUpdated = persistedScores.size();
        if (teamsUpdated != TEAMS_PER_FIXTURE) {
            throw new BusinessRuleException("Two team scores are required before the fixture can be processed.");
        }

        boolean leaderboardsRefreshed = refreshLeaderboards(actorUserId, fixture);
        if (!leaderboardsRefreshed) {
            throw new BusinessRuleException("Leaderboard refresh did not complete.");
        }

        fixture.setStatus(FixtureStatus.PROCESSED);
        if (!fixtureDAO.updateFixture(fixture)) {
            throw new BusinessRuleException("The fixture status could not be updated.");
        }

        LOG.log(Level.INFO, "Fixture processed successfully.");
        return MatchProcessingResultDTO.builder()
                .fixtureId(fixtureId)
                .pointsCalculated(statistics.size())
                .teamsUpdated(teamsUpdated)
                .leaderboardsRefreshed(leaderboardsRefreshed)
                .status(fixture.getStatus().name())
                .build();
    }

    private void requireAdmin(UUID actorUserId) {
        if (adminDAO.getAdminById(actorUserId).isEmpty()) {
            throw new AuthorisationException("Only administrators may process match results.");
        }
    }

    private boolean refreshLeaderboards(UUID actorUserId, Fixture fixture) {
        if (fixture.getLeagueId() == null) {
            return false;
        }

        LeaderboardRefreshResultDTO refresh = leaderboardService.refreshLeagueLeaderboard(actorUserId, fixture.getLeagueId());
        return refresh != null && refresh.isSuccess();
    }
}
