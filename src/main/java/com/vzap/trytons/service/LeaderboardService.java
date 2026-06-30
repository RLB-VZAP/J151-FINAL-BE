package com.vzap.trytons.service;

import com.vzap.trytons.model.Leaderboard;
import com.vzap.trytons.model.Ranking;

import java.util.Optional;
import java.util.UUID;

public interface LeaderboardService {
    Optional<Leaderboard> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId);
    Optional<Ranking>  getRankingForTeam(UUID teamId, UUID leaderboardId);
}
