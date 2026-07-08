package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.PlayerDAO;
import com.vzap.trytons.dao.RegisteredUserDAO;
import com.vzap.trytons.dto.*;
import com.vzap.trytons.exceptions.BadRequestException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class FantasyTeamServiceImpl implements FantasyTeamService {
    private static final BigDecimal INITIAL_BUDGET = BigDecimal.valueOf(100000000.00);

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private RegisteredUserDAO registeredUserDAO;

    @Inject
    private PlayerDAO playerDAO;

    @Override
    public FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO fantasyTeamDTO) {
        FantasyTeam fantasyTeam = mapRequestToFantasyTeam(fantasyTeamDTO);

        fantasyTeam.setTeamId(UUID.randomUUID());
        fantasyTeam.setOwner(registeredUserDAO.getRegisteredUserById(registeredUserId).orElseThrow(() -> new RuntimeException("Register User not found.")));
        fantasyTeam.setTotalTeamValue(totalTeamValue);
        fantasyTeam.setRemainingBudget(INITIAL_BUDGET);
        fantasyTeam.setCreationDate(LocalDateTime.now());
        fantasyTeam.setTotalPoints(0);
        fantasyTeam.setWeeklyPoints(0);
        fantasyTeam.setIsValid(true);
        fantasyTeam.setIsLocked(false);

        return null;
    }

    @Override
    public ViewOpponentTeamDTO viewOpponentTeam(UUID teamId) {
        return null;
    }

    @Override
    public ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId) {
        return null;
    }

    @Override
    public FantasyTeamResponseDTO updateTeam(UUID registeredId, UUID teamId, FantasyTeamRequestDTO fantasyTeamDTO) {
        return null;
    }

    private FantasyTeam mapRequestToFantasyTeam(FantasyTeamRequestDTO request) {
        FantasyTeam fantasyTeam = new FantasyTeam();
        fantasyTeam.setTeamName(request.getTeamName().trim());

        return fantasyTeam;
    }

    private BigDecimal totalTeamValue(FantasyTeamRequestDTO request) {
        int teamValue = BigDecimal.ZERO;
        try {
            for(FantasyTeamPlayerSelectionRequestDTO playerSelectionDTO : request.getSelectedPlayers()) {
                Player player = playerDAO.getPlayerById(playerSelectionDTO.getPlayerId()).orElseThrow(() -> new RuntimeException("Player not found."));
            }
            return teamValue;
        }catch(RuntimeException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
