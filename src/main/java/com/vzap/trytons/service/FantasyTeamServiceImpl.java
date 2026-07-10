package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dto.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.FantasyTeamResponseDTO;
import com.vzap.trytons.dto.ViewOpponentTeamDTO;
import com.vzap.trytons.dto.ViewOwnTeamDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.RegisteredUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@ApplicationScoped
public class FantasyTeamServiceImpl implements FantasyTeamService {

    private static final BigDecimal STARTING_BUDGET = new BigDecimal("100.00");

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Override
    public FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO request) {
        validateUserId(registeredUserId);
        validateRequest(request);

        RegisteredUser owner = new RegisteredUser();
        owner.setUserId(registeredUserId);

        FantasyTeam team = new FantasyTeam();
        team.setTeamId(UUID.randomUUID());
        team.setTeamName(request.getTeamName().trim());
        team.setOwner(owner);
        team.setRemainingBudget(STARTING_BUDGET);
        team.setTotalTeamValue(BigDecimal.ZERO);
        team.setCreationDate(LocalDateTime.now());
        team.setTotalPoints(0);
        team.setWeeklyPoints(0);
        team.setIsValid(false);
        team.setIsLocked(false);

        FantasyTeam savedTeam = fantasyTeamDAO.createTeam(team)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team could not be created"));

        return toResponse(savedTeam);
    }

    @Override
    public ViewOpponentTeamDTO viewOpponentTeam(UUID teamId) {
        FantasyTeam team = getRequiredTeam(teamId);

        return new ViewOpponentTeamDTO(
                team.getTeamId(),
                team.getTeamName(),
                team.getTotalPoints(),
                team.getWeeklyPoints(),
                Collections.emptyList()
        );
    }

    @Override
    public ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId) {
        validateUserId(registeredUserId);
        FantasyTeam team = getRequiredTeam(teamId);
        requireOwner(team, registeredUserId);

        return new ViewOwnTeamDTO(
                team.getTeamId(),
                team.getTeamName(),
                safeMoney(team.getTotalTeamValue()),
                safeMoney(team.getRemainingBudget()),
                team.getCreationDate(),
                team.getTotalPoints(),
                team.getWeeklyPoints(),
                team.getIsValid(),
                team.getIsLocked(),
                team.getOwner() != null ? team.getOwner().getUsername() : null,
                Collections.emptyList()
        );
    }

    @Override
    public FantasyTeamResponseDTO updateTeam(UUID registeredUserId, UUID teamId, FantasyTeamRequestDTO request) {
        validateUserId(registeredUserId);
        validateRequest(request);

        FantasyTeam team = getRequiredTeam(teamId);
        requireOwner(team, registeredUserId);

        // The current DAO contract does not expose an update-team-name method yet.
        // Returning the existing team keeps the backend deployable until that DAO method is added.
        team.setTeamName(request.getTeamName().trim());
        return toResponse(team);
    }

    private FantasyTeam getRequiredTeam(UUID teamId) {
        if (teamId == null) {
            throw new ValidationException("Team ID is required");
        }

        return fantasyTeamDAO.getTeamById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found"));
    }

    private void validateUserId(UUID registeredUserId) {
        if (registeredUserId == null) {
            throw new ValidationException("Registered user ID is required");
        }
    }

    private void validateRequest(FantasyTeamRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Fantasy team request is required");
        }

        if (request.getTeamName() == null || request.getTeamName().trim().isEmpty()) {
            throw new ValidationException("Team name is required");
        }
    }

    private void requireOwner(FantasyTeam team, UUID registeredUserId) {
        if (team.getOwner() == null || team.getOwner().getUserId() == null
                || !team.getOwner().getUserId().equals(registeredUserId)) {
            throw new AuthorisationException("You do not own this fantasy team");
        }
    }

    private FantasyTeamResponseDTO toResponse(FantasyTeam team) {
        return new FantasyTeamResponseDTO(
                team.getTeamId(),
                team.getTeamName(),
                team.getOwner() != null ? team.getOwner().getUserId() : null,
                team.getOwner() != null ? team.getOwner().getUsername() : null,
                safeMoney(team.getTotalTeamValue()),
                safeMoney(team.getRemainingBudget()),
                team.getWeeklyPoints(),
                team.getTotalPoints(),
                Collections.emptyList()
        );
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
