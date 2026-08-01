package com.vzap.trytons.dao.leaderboard;

import com.vzap.trytons.util.LeaderboardAggregator;

import java.util.List;
import java.util.UUID;

/**
 * Supplies the per-team, per-fixture score rows {@link LeaderboardAggregator}
 * rolls up into season totals. This is the query
 * {@code LeaderboardDAO.saveRanking}/{@code updateRanking} never had a
 * caller to feed: ranking.total_fantasy_points was never computed anywhere
 * in Java before this.
 */
public interface LeaderboardAggregationDAO {

    /** PUBLIC-league matches only, across every league, for the season -- the master total. */
    List<LeaderboardAggregator.FixtureScoreRow> findMasterScoreRows(String season);

    /** Every completed match in one league, public or private -- that league's own table. */
    List<LeaderboardAggregator.FixtureScoreRow> findLeagueScoreRows(String season, UUID leagueId);
}
