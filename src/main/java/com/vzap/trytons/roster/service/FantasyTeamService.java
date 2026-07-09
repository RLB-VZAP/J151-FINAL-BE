package com.vzap.trytons.roster.service;

import com.vzap.trytons.roster.dto.FantasyTeamRequestDTO;
import com.vzap.trytons.roster.dto.FantasyTeamResponseDTO;
import com.vzap.trytons.roster.dto.ViewOpponentTeamDTO;
import com.vzap.trytons.roster.dto.ViewOwnTeamDTO;

import java.util.UUID;

public interface FantasyTeamService {
    FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO fantasyTeamDTO);
    ViewOpponentTeamDTO viewOpponentTeam(UUID teamId);
    ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId);
    FantasyTeamResponseDTO updateTeam(UUID registeredId, UUID teamId, FantasyTeamRequestDTO fantasyTeamDTO);
}