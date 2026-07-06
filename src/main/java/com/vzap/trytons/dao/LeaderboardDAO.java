package com.vzap.trytons.dao;

import com.vzap.trytons.model.Leaderboard;
import com.vzap.trytons.model.Ranking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaderboardDAO {
    Optional<Leaderboard> getLeaderboardByLeagueId(UUID leagueId);
    List<Ranking> getRankingsByLeaderboardId(UUID leaderboardId);
    Optional<Ranking> getRankingByTeamId(UUID teamId, UUID leaderboardId);
    Optional<Leaderboard> getLeaderboardById(UUID leaderboardId);
}
