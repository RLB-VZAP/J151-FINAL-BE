package com.vzap.trytons.service;

import com.vzap.trytons.dto.ViewOpponentTeamDTO;
import com.vzap.trytons.dto.ViewOwnTeamDTO;
import com.vzap.trytons.model.FantasyTeam;

import java.util.UUID;

public interface FantasyTeamService {
    //TODO Import FantasyTeamDTO when available
    FantasyTeam createTeam(UUID registeredUserId,FantasyTeamDTO fantasyTeamDTO);
    ViewOpponentTeamDTO viewOpponentTeam(UUID teamId);
    ViewOwnTeamDTO viewOwnTeam(UUID registeredUserId, UUID teamId);
    FantasyTeam updateTeam(UUID registeredId,  UUID teamId, FantasyTeamDTO fantasyTeamDTO);
}