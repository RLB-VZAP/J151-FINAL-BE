package com.vzap.trytons.service;

import com.vzap.trytons.dto.LeaderboardEntryResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaderboardService {
    List<LeaderboardEntryResponseDTO> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId);
    Optional<LeaderboardEntryResponseDTO>  getRankingForTeam(UUID teamId, UUID leaderboardId, UUID requestingUserId);
}
