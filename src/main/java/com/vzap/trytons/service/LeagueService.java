package com.vzap.trytons.service;

import com.vzap.trytons.dto.LeagueRequestDTO;
import com.vzap.trytons.dto.LeagueResponseDTO;

import java.util.UUID;

public interface LeagueService {

    LeagueResponseDTO createLeague(LeagueResponseDTO createLeague, LeagueRequestDTO request, UUID currentUserId);

    LeagueResponseDTO getLeague(UUID leagueId,UUID currentUserId);

    void joinLeague(UUID leagueId, UUID currentUserId, UUID fantasyTeamId);

    boolean isLeagueMember(UUID leagueId, UUID userId);
}
