package com.vzap.trytons.dao.shared;

/**
 * Shared filter for every direct reader that totals a player's fantasy
 * points by walking {@code playerStatistics -> matchResult -> fixture ->
 * league} instead of going through {@code ranking}. Private leagues are
 * friendlies -- real results, real fantasy points -- but those results must
 * not move player prices, market demand, or a player's counted total.
 *
 * <p>Five call sites need this exact join: {@code PricingMetricsDAOImpl},
 * {@code MarketDemandDAOImpl} and both queries in {@code FantasyPointsDAOImpl}.
 * Defined once here so they can't drift from each other.
 *
 * <p>Assumes the enclosing query aliases {@code playerStatistics} as
 * {@code ps}; append with {@code " AND "}.
 */
public final class PublicLeagueScope {

    private PublicLeagueScope() {
    }

    public static final String PLAYER_STATISTICS_FILTER =
            "EXISTS (SELECT 1 FROM matchResult mr_scope "
            + "JOIN fixture f_scope ON f_scope.fixtureId = mr_scope.fixtureId "
            + "JOIN league l_scope ON l_scope.leagueId = f_scope.leagueId "
            + "WHERE mr_scope.resultId = ps.resultId AND l_scope.leagueType = 'PUBLIC')";
}
