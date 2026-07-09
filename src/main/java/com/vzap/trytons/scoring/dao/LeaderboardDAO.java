package com.vzap.trytons.scoring.dao;

import com.vzap.trytons.scoring.model.Leaderboard;
import com.vzap.trytons.scoring.model.Ranking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaderboardDAO {
    Optional<Leaderboard> getLeaderboardByLeagueId(UUID leagueId);
    List<Ranking> getRankingsByLeaderboardId(UUID leaderboardId);
    Optional<Ranking> getRankingByTeamId(UUID teamId, UUID leaderboardId);
    Optional<Leaderboard> getLeaderboardById(UUID leaderboardId);
    void saveRanking(Ranking ranking);
    void updateRanking(Ranking ranking);
    void deleteRankingByLeaderboardId(UUID leaderboardId);
    void updateLeaderboard(Leaderboard leaderboard);
    void saveLeaderboard(Leaderboard leaderboard);
    Optional<Leaderboard> getMasterLeaderboard();
}
