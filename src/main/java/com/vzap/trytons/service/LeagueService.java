package com.vzap.trytons.service;

import com.vzap.trytons.dto.LeagueRequestDTO;
import com.vzap.trytons.dto.LeagueResponseDTO;

import java.util.List;
import java.util.UUID;

public interface LeagueService {

    LeagueResponseDTO createLeague(LeagueRequestDTO request, UUID currentUserId);

    LeagueResponseDTO getLeague(UUID leagueId,UUID currentUserId);

    void joinLeague(UUID leagueId, UUID currentUserId, UUID fantasyTeamId);

    boolean isLeagueMember(UUID leagueId, UUID userId);

    List<LeagueResponseDTO> getAllLeagues(UUID currentUserId);
}
