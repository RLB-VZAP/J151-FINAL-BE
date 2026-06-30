package com.vzap.trytons.service;

import com.vzap.trytons.dao.LeaderboardDAO;
import com.vzap.trytons.model.Leaderboard;
import com.vzap.trytons.model.Ranking;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.UUID;

public class LeaderboardServiceImpl implements LeaderboardService{
    @Inject
    private LeaderboardDAO leaderboardDAO;

    @Override
    public Optional<Leaderboard> getLeaderboardForLeague(UUID leagueId, UUID requestingUserId) {
        return Optional.empty();
    }

    @Override
    public Optional<Ranking> getRankingForTeam(UUID teamId, UUID leaderboardId) {
        return Optional.empty();
    }
}
