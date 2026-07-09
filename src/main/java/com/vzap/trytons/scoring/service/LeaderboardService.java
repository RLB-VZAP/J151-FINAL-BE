package com.vzap.trytons.scoring.service;

import com.vzap.trytons.scoring.dto.LeaderboardRefreshResultDTO;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.vzap.trytons.scoring.dto.LeaderboardEntryResponseDTO;

public interface LeaderboardService {
    LeaderboardRefreshResultDTO refreshLeagueLeaderboard(UUID actorUserId, UUID leagueId);
    LeaderboardRefreshResultDTO refreshOverallLeaderboard(UUID actorUserId);
    List<LeaderboardEntryResponseDTO> getOverallLeaderboard(UUID actorUserId);
    List<LeaderboardEntryResponseDTO> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId);
    Optional<LeaderboardEntryResponseDTO>  getRankingForTeam(UUID teamId, UUID leaderboardId, UUID requestingUserId);
}
