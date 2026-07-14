package com.vzap.trytons.service;

import com.vzap.trytons.dao.FixtureDAO;
import com.vzap.trytons.dao.MatchResultDAO;
import com.vzap.trytons.dao.UserDAO;
import com.vzap.trytons.dto.MatchResultRequestDTO;
import com.vzap.trytons.dto.MatchResultResponseDTO;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.MatchTeamSide;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.MatchResult;
import com.vzap.trytons.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class MatchResultServiceImpl implements MatchResultService {
    private static final Set<FixtureStatus> CAPTURABLE_STATES = EnumSet.of(FixtureStatus.LOCKED, FixtureStatus.SIMULATING, FixtureStatus.COMPLETED);
    private final MatchResultDAO matchResultDAO;
    private final FixtureDAO fixtureDAO;
    private final UserDAO userDAO;

    @Inject
    public MatchResultServiceImpl(MatchResultDAO matchResultDAO, FixtureDAO fixtureDAO, UserDAO userDAO) {
        this.matchResultDAO = matchResultDAO;
        this.fixtureDAO = fixtureDAO;
        this.userDAO = userDAO;
    }

    @Override
    public MatchResultResponseDTO captureResult(UUID actorUserId, MatchResultRequestDTO request) {
        validateRequest(request);
        requireAdmin(actorUserId);

        Fixture fixture = fixtureDAO.findById(request.getFixtureId()).orElseThrow(() -> new ResourceNotFoundException("Fixture was not found."));

        if (!CAPTURABLE_STATES.contains(fixture.getStatus())) {
            throw new ConflictException("A match result cannot be captured while the fixture is " + fixture.getStatus());
        }

        int simulationRunNumber = matchResultDAO.getNextSimulationRunNumber(fixture.getFixtureId());

        matchResultDAO.markAllFixtureResultsNotCurrent(fixture.getFixtureId());

        MatchResult saved = matchResultDAO.save(buildResult(fixture, request, simulationRunNumber));

        if (saved == null) {
            throw new DataAccessException("Failed to persist the captured match result.", null);
        }

        return mapToResponse(saved);
    }

    @Override
    public MatchResultResponseDTO getResult(UUID fixtureId) {
        if (fixtureId == null) {
            throw new ValidationException("Fixture ID is required.");
        }

        MatchResult result = matchResultDAO.findCurrentByFixtureId(fixtureId).orElseThrow(() -> new ResourceNotFoundException("No match result exists for the fixture."));
        return mapToResponse(result);
    }

    private void validateRequest(MatchResultRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Match result details are required.");
        }
        if (request.getFixtureId() == null) {
            throw new ValidationException("Fixture ID is required.");
        }
        if (request.getTeamAScore() < 0 || request.getTeamBScore() < 0) {
            throw new ValidationException("Match scores cannot be negative.");
        }
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required to capture match results.");
        }
        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("An authenticated administrator is required to capture match results."));
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may capture or correct match results.");
        }
    }

    private MatchResult buildResult(Fixture fixture, MatchResultRequestDTO request, int simulationRunNumber) {
        int teamAScore = request.getTeamAScore();
        int teamBScore = request.getTeamBScore();

        return MatchResult.builder()
                .resultId(UUID.randomUUID())
                .fixtureId(fixture.getFixtureId())
                .teamAId(fixture.getTeamA() != null ? fixture.getTeamA().getTeamId() : null)
                .teamBId(fixture.getTeamB() != null ? fixture.getTeamB().getTeamId() : null)
                .simulationRunNumber(simulationRunNumber)
                .teamAScore(teamAScore)
                .teamBScore(teamBScore)
                .winnerSide(resolveWinnerSide(teamAScore, teamBScore))
                .draw(teamAScore == teamBScore)
                .approved(false)
                .current(true)
                .resultDate(LocalDateTime.now())
                .approvedAt(null)
                .approvedByAdminId(null)
                .build();
    }

    private String resolveWinnerSide(int teamAScore, int teamBScore) {
        if (teamAScore == teamBScore) {
            return null;
        }
        return teamAScore > teamBScore ? MatchTeamSide.TEAM_A.name() : MatchTeamSide.TEAM_B.name();
    }

    private MatchResultResponseDTO mapToResponse(MatchResult result) {
        return new MatchResultResponseDTO(
                result.getResultId(),
                result.getFixtureId(),
                result.getTeamAId(),
                result.getTeamBId(),
                result.getSimulationRunNumber(),
                result.getTeamAScore(),
                result.getTeamBScore(),
                result.getWinnerSide(),
                result.isDraw(),
                result.isApproved(),
                result.isCurrent(),
                result.getResultDate(),
                result.getApprovedAt());
    }
}
