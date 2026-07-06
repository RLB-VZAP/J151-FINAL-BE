package com.vzap.trytons.service;

import com.vzap.trytons.dto.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.FantasyTeamResponseDTO;
import com.vzap.trytons.dto.ViewOpponentTeamDTO;
import com.vzap.trytons.dto.ViewOwnTeamDTO;

import java.util.UUID;

public interface FantasyTeamService {
    FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO fantasyTeamDTO);
    ViewOpponentTeamDTO viewOpponentTeam(UUID teamId);
    ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId);
    FantasyTeamResponseDTO updateTeam(UUID registeredId, UUID teamId, FantasyTeamRequestDTO fantasyTeamDTO);
}