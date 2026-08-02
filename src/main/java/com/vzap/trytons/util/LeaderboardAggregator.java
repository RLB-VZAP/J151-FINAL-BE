package com.vzap.trytons.util;

import com.vzap.trytons.enums.LeagueType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rolls per-match team scores up into season totals, applying the rule that
 * only PUBLIC-league results count toward a manager's master total. Private
 * leagues are friendlies: they keep producing full results (three database
 * triggers require it), but those results must stop at the league's own
 * table.
 *
 * <p>The database query supplies one {@link FixtureScoreRow} per team per
 * completed fixture; this class does the roll-up in Java so the exclusion
 * rule is testable without a database. {@link Scope#MASTER} filters rows to
 * {@link LeagueType#PUBLIC}; {@link Scope#LEAGUE} keeps everything it is
 * given, since a private league's own table must still count its own
 * matches.
 *
 * <p>Points-for-a-result mirror {@code seed-presentation.sql}'s hand-computed
 * ranking rows: four league points for a win, two for a draw, none for a
 * loss.
 */
public final class LeaderboardAggregator {

    private LeaderboardAggregator() {
    }

    public static final int WIN_LEAGUE_POINTS = 4;
    public static final int DRAW_LEAGUE_POINTS = 2;
    public static final int LOSS_LEAGUE_POINTS = 0;

    public enum Scope {
        MASTER,
        LEAGUE
    }

    /** One team's side of one completed, PROCESSED fixture. */
    public record FixtureScoreRow(UUID teamId, LeagueType leagueType, int pointsFor, int pointsAgainst) {
    }

    /** A manager's rolled-up season totals, ready to persist onto {@code ranking}. */
    public static final class TeamTotals {
        private final UUID teamId;
        private int matchesPlayed;
        private int matchesWon;
        private int matchesDrawn;
        private int matchesLost;
        private int pointsFor;
        private int pointsAgainst;
        private int leaguePoints;
        private int totalFantasyPoints;

        private TeamTotals(UUID teamId) {
            this.teamId = teamId;
        }

        private void apply(FixtureScoreRow row) {
            matchesPlayed++;
            pointsFor += row.pointsFor();
            pointsAgainst += row.pointsAgainst();
            totalFantasyPoints += row.pointsFor();

            int margin = row.pointsFor() - row.pointsAgainst();
            if (margin > 0) {
                matchesWon++;
                leaguePoints += WIN_LEAGUE_POINTS;
            } else if (margin == 0) {
                matchesDrawn++;
                leaguePoints += DRAW_LEAGUE_POINTS;
            } else {
                matchesLost++;
                leaguePoints += LOSS_LEAGUE_POINTS;
            }
        }

        public UUID getTeamId() {
            return teamId;
        }

        public int getMatchesPlayed() {
            return matchesPlayed;
        }

        public int getMatchesWon() {
            return matchesWon;
        }

        public int getMatchesDrawn() {
            return matchesDrawn;
        }

        public int getMatchesLost() {
            return matchesLost;
        }

        public int getPointsFor() {
            return pointsFor;
        }

        public int getPointsAgainst() {
            return pointsAgainst;
        }

        public int getScoreDifference() {
            return pointsFor - pointsAgainst;
        }

        public int getLeaguePoints() {
            return leaguePoints;
        }

        public int getTotalFantasyPoints() {
            return totalFantasyPoints;
        }
    }

    /**
     * Rolls the given rows up by team, applying the scope's inclusion rule.
     * Team order in the result follows first appearance in {@code rows}; use
     * {@link #rankingOrder(Scope)} to sort for display.
     */
    public static List<TeamTotals> aggregate(List<FixtureScoreRow> rows, Scope scope) {
        Map<UUID, TeamTotals> byTeam = new LinkedHashMap<>();

        for (FixtureScoreRow row : rows) {
            if (scope == Scope.MASTER && row.leagueType() != LeagueType.PUBLIC) {
                continue;
            }
            byTeam.computeIfAbsent(row.teamId(), TeamTotals::new).apply(row);
        }

        return new ArrayList<>(byTeam.values());
    }

    /**
     * Mirrors {@code LeaderboardServiceImpl.refreshRankings}' comparator
     * choice, which now differs by scope: MASTER ranks on total fantasy
     * points (the master leaderboard is a fantasy-points competition), while
     * LEAGUE keeps the original league-table ordering (league points from
     * win/draw/loss, then score difference, then total fantasy points).
     * Keep this in sync with {@code refreshRankings} -- the two must agree.
     */
    public static Comparator<TeamTotals> rankingOrder(Scope scope) {
        if (scope == Scope.MASTER) {
            return Comparator.comparingInt(TeamTotals::getTotalFantasyPoints).reversed()
                    .thenComparing(Comparator.comparingInt(TeamTotals::getLeaguePoints).reversed())
                    .thenComparing(Comparator.comparingInt(TeamTotals::getScoreDifference).reversed());
        }
        return Comparator.comparingInt(TeamTotals::getLeaguePoints).reversed()
                .thenComparing(Comparator.comparingInt(TeamTotals::getScoreDifference).reversed())
                .thenComparing(Comparator.comparingInt(TeamTotals::getTotalFantasyPoints).reversed());
    }

    /** An all-zero totals row for a team the aggregation returned nothing for. */
    public static TeamTotals zero(UUID teamId) {
        return new TeamTotals(teamId);
    }

    /**
     * Guarantees a totals entry for every team a leaderboard already tracks,
     * even when the fresh aggregation found no qualifying row for it this
     * time. A "refresh" that only ever updates the teams present in the new
     * aggregation is a merge, not a recompute: a team whose only results
     * turned private (or were removed) would keep its last total forever.
     * {@code existingTeamIds} are zeroed in; teams already present in
     * {@code totals} are left untouched. Teams in {@code totals} with no
     * existing row (their first-ever counted result) pass through unchanged,
     * for the caller to insert.
     */
    public static Map<UUID, TeamTotals> reconcileWithExisting(List<TeamTotals> totals,
                                                                Collection<UUID> existingTeamIds) {
        Map<UUID, TeamTotals> byTeam = new LinkedHashMap<>();
        for (TeamTotals teamTotals : totals) {
            byTeam.put(teamTotals.getTeamId(), teamTotals);
        }
        for (UUID teamId : existingTeamIds) {
            byTeam.putIfAbsent(teamId, zero(teamId));
        }
        return byTeam;
    }
}
