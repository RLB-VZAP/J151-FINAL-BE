package com.vzap.trytons.service.fantasyteam;

import com.vzap.trytons.dto.fantasyteam.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.fantasyteam.FantasyTeamResponseDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOpponentTeamDTO;
import com.vzap.trytons.dto.fantasyteam.ViewOwnTeamDTO;

import java.util.UUID;

public interface FantasyTeamService {
    FantasyTeamResponseDTO createTeam(UUID registeredUserId, FantasyTeamRequestDTO fantasyTeamDTO);

    /**
     * The caller's own team, or null when they have not created one.
     *
     * uk_fantasyTeam_owner allows one team per user, so the caller identifies the
     * team on its own. Without this, clients had no way to discover their own team
     * id: every other lookup already requires it.
     */
    FantasyTeamResponseDTO getOwnTeam(UUID registeredUserId);

    ViewOpponentTeamDTO viewOpponentTeam(UUID teamId);
    ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId);
    FantasyTeamResponseDTO updateTeam(UUID registeredId, UUID teamId, FantasyTeamRequestDTO fantasyTeamDTO);
}