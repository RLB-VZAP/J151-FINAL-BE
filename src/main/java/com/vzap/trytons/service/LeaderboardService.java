package com.vzap.trytons.service;

import com.vzap.trytons.dto.LeaderboardRefreshResultDTO;

import java.util.UUID;

public interface LeaderboardService {
    LeaderboardRefreshResultDTO refreshLeagueLeaderboard(UUID actorUserId, UUID leagueId);
    LeaderboardRefreshResultDTO refreshOverallLeaderboard(UUID actorUserId);
}
