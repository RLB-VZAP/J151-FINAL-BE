package com.vzap.trytons.util;

import com.vzap.trytons.enums.LeagueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LeaderboardAggregator is where the "public leagues count toward a
 * manager's global total, private leagues don't" rule actually lives, since
 * the SQL that feeds it filters at the database layer and can't be pinned by
 * a unit test on its own. These tests exercise the same exclusion in Java,
 * against {@code FixtureScoreRow}s shaped exactly like the aggregation DAO
 * would hand back.
 */
class LeaderboardAggregatorTest {

    private static final UUID TEAM_A = UUID.randomUUID();
    private static final UUID TEAM_B = UUID.randomUUID();

    private static LeaderboardAggregator.FixtureScoreRow row(
            UUID teamId, LeagueType leagueType, int pointsFor, int pointsAgainst) {
        return new LeaderboardAggregator.FixtureScoreRow(teamId, leagueType, pointsFor, pointsAgainst);
    }

    private static LeaderboardAggregator.TeamTotals find(
            List<LeaderboardAggregator.TeamTotals> totals, UUID teamId) {
        return totals.stream()
                .filter(t -> t.getTeamId().equals(teamId))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("a private-league result is excluded from the master roll-up")
    void privateResultExcludedFromMaster() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                row(TEAM_A, LeagueType.PRIVATE, 30, 10));

        List<LeaderboardAggregator.TeamTotals> totals =
                LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER);

        assertTrue(totals.isEmpty());
    }

    @Test
    @DisplayName("a private-league result IS included in that league's own roll-up")
    void privateResultIncludedInLeagueScope() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                row(TEAM_A, LeagueType.PRIVATE, 30, 10));

        List<LeaderboardAggregator.TeamTotals> totals =
                LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.LEAGUE);

        LeaderboardAggregator.TeamTotals teamA = find(totals, TEAM_A);
        assertEquals(1, teamA.getMatchesPlayed());
        assertEquals(30, teamA.getPointsFor());
    }

    @Test
    @DisplayName("a team playing in both a public and a private league is counted correctly per scope")
    void teamInBothLeagueTypesCountedPerScope() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                row(TEAM_A, LeagueType.PUBLIC, 20, 10),
                row(TEAM_A, LeagueType.PRIVATE, 30, 15));

        List<LeaderboardAggregator.TeamTotals> master =
                LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER);
        LeaderboardAggregator.TeamTotals masterTotals = find(master, TEAM_A);
        assertEquals(1, masterTotals.getMatchesPlayed());
        assertEquals(20, masterTotals.getPointsFor());
        assertEquals(10, masterTotals.getPointsAgainst());

        // Passing only the private-league rows mirrors how the DAO would
        // scope a league query to one leagueId: the private match still
        // counts in that league's own table.
        List<LeaderboardAggregator.FixtureScoreRow> privateOnly = List.of(rows.get(1));
        List<LeaderboardAggregator.TeamTotals> leagueScope =
                LeaderboardAggregator.aggregate(privateOnly, LeaderboardAggregator.Scope.LEAGUE);
        LeaderboardAggregator.TeamTotals leagueTotals = find(leagueScope, TEAM_A);
        assertEquals(1, leagueTotals.getMatchesPlayed());
        assertEquals(30, leagueTotals.getPointsFor());
        assertEquals(15, leagueTotals.getPointsAgainst());
    }

    @Test
    @DisplayName("a manager with only private results totals zero on the master roll-up")
    void managerWithOnlyPrivateResultsTotalsZeroOnMaster() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                row(TEAM_A, LeagueType.PRIVATE, 18, 15),
                row(TEAM_A, LeagueType.PRIVATE, 12, 20));

        List<LeaderboardAggregator.TeamTotals> master =
                LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER);

        assertTrue(master.isEmpty());
    }

    @Test
    @DisplayName("win, draw and loss are counted and scored correctly")
    void winDrawLossArithmetic() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                row(TEAM_A, LeagueType.PUBLIC, 20, 10),  // win
                row(TEAM_A, LeagueType.PUBLIC, 14, 14),  // draw
                row(TEAM_A, LeagueType.PUBLIC, 10, 20)); // loss

        LeaderboardAggregator.TeamTotals totals =
                find(LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER), TEAM_A);

        assertEquals(3, totals.getMatchesPlayed());
        assertEquals(1, totals.getMatchesWon());
        assertEquals(1, totals.getMatchesDrawn());
        assertEquals(1, totals.getMatchesLost());
        assertEquals(44, totals.getPointsFor());
        assertEquals(44, totals.getPointsAgainst());
        assertEquals(0, totals.getScoreDifference());
        // One win (4) + one draw (2) + one loss (0).
        assertEquals(6, totals.getLeaguePoints());
        assertEquals(44, totals.getTotalFantasyPoints());
    }

    @Test
    @DisplayName("pointsFor, pointsAgainst and score difference accumulate across matches")
    void pointsForAgainstAndDifferenceAccumulate() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                row(TEAM_A, LeagueType.PUBLIC, 25, 12),
                row(TEAM_A, LeagueType.PUBLIC, 18, 30));

        LeaderboardAggregator.TeamTotals totals =
                find(LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER), TEAM_A);

        assertEquals(43, totals.getPointsFor());
        assertEquals(42, totals.getPointsAgainst());
        assertEquals(1, totals.getScoreDifference());
    }

    @Test
    @DisplayName("LEAGUE ranking order mirrors refreshRankings' LEAGUE branch: league points, then score difference, then total fantasy points")
    void rankingOrderMirrorsRefreshRankingsLeagueScope() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                // TEAM_A: one win, big margin -- highest league points.
                row(TEAM_A, LeagueType.PUBLIC, 40, 10),
                // TEAM_B: one draw -- fewer league points than a win, so
                // ranks below TEAM_A regardless of points scored.
                row(TEAM_B, LeagueType.PUBLIC, 50, 50));

        List<LeaderboardAggregator.TeamTotals> totals =
                LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER);
        totals.sort(LeaderboardAggregator.rankingOrder(LeaderboardAggregator.Scope.LEAGUE));

        assertEquals(TEAM_A, totals.get(0).getTeamId());
        assertEquals(TEAM_B, totals.get(1).getTeamId());
    }

    @Test
    @DisplayName("LEAGUE scope: when league points tie, score difference breaks the tie")
    void scoreDifferenceBreaksLeaguePointsTie() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                row(TEAM_A, LeagueType.PUBLIC, 30, 10),  // win, difference +20
                row(TEAM_B, LeagueType.PUBLIC, 20, 15)); // win, difference +5

        List<LeaderboardAggregator.TeamTotals> totals =
                LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER);
        totals.sort(LeaderboardAggregator.rankingOrder(LeaderboardAggregator.Scope.LEAGUE));

        assertEquals(TEAM_A, totals.get(0).getTeamId());
        assertEquals(TEAM_B, totals.get(1).getTeamId());
    }

    @Test
    @DisplayName("MASTER and LEAGUE scopes rank the same two teams in OPPOSITE order: "
            + "a team with fewer league points but far more fantasy points tops MASTER, not LEAGUE")
    void masterAndLeagueScopesDisagreeOnOrder() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                // TEAM_A: a heavy loss on fantasy points -- zero league points, but a
                // massive fantasy haul (100).
                row(TEAM_A, LeagueType.PUBLIC, 100, 120),
                // TEAM_B: a modest win -- full league points (4), but a small fantasy
                // haul (50).
                row(TEAM_B, LeagueType.PUBLIC, 50, 10));

        List<LeaderboardAggregator.TeamTotals> totals =
                LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER);

        // MASTER ranks on total fantasy points first: TEAM_A (100) beats TEAM_B (50)
        // despite having lost its match.
        List<LeaderboardAggregator.TeamTotals> masterOrdered = new java.util.ArrayList<>(totals);
        masterOrdered.sort(LeaderboardAggregator.rankingOrder(LeaderboardAggregator.Scope.MASTER));
        assertEquals(TEAM_A, masterOrdered.get(0).getTeamId());
        assertEquals(TEAM_B, masterOrdered.get(1).getTeamId());

        // LEAGUE ranks on league points first: TEAM_B (4, a win) beats TEAM_A (0, a
        // loss) regardless of fantasy points scored -- the opposite order.
        List<LeaderboardAggregator.TeamTotals> leagueOrdered = new java.util.ArrayList<>(totals);
        leagueOrdered.sort(LeaderboardAggregator.rankingOrder(LeaderboardAggregator.Scope.LEAGUE));
        assertEquals(TEAM_B, leagueOrdered.get(0).getTeamId());
        assertEquals(TEAM_A, leagueOrdered.get(1).getTeamId());
    }

    @Test
    @DisplayName("a recompute zeroes a team that has no qualifying rows this pass, instead of keeping its old total")
    void recomputeZeroesTeamWithNoQualifyingRows() {
        // TEAM_B previously had a real total (e.g. it used to have a public
        // result, or the leaderboard was seeded with one). This pass's
        // aggregation -- already scope-filtered -- found nothing for it, so
        // it's simply missing from `totals`. That must not be read as
        // "leave it alone": a real recompute overwrites it with zero.
        List<LeaderboardAggregator.TeamTotals> totals = LeaderboardAggregator.aggregate(
                List.of(row(TEAM_A, LeagueType.PUBLIC, 20, 10)),
                LeaderboardAggregator.Scope.MASTER);

        Map<UUID, LeaderboardAggregator.TeamTotals> reconciled =
                LeaderboardAggregator.reconcileWithExisting(totals, List.of(TEAM_A, TEAM_B));

        LeaderboardAggregator.TeamTotals teamB = reconciled.get(TEAM_B);
        assertEquals(0, teamB.getMatchesPlayed());
        assertEquals(0, teamB.getMatchesWon());
        assertEquals(0, teamB.getMatchesDrawn());
        assertEquals(0, teamB.getMatchesLost());
        assertEquals(0, teamB.getPointsFor());
        assertEquals(0, teamB.getPointsAgainst());
        assertEquals(0, teamB.getLeaguePoints());
        assertEquals(0, teamB.getTotalFantasyPoints());

        // TEAM_A, which did have a qualifying row this pass, is untouched.
        LeaderboardAggregator.TeamTotals teamA = reconciled.get(TEAM_A);
        assertEquals(1, teamA.getMatchesPlayed());
        assertEquals(20, teamA.getPointsFor());
    }

    @Test
    @DisplayName("LEAGUE scope: when league points and score difference tie, total fantasy points breaks the tie")
    void totalFantasyPointsBreaksRemainingTie() {
        List<LeaderboardAggregator.FixtureScoreRow> rows = List.of(
                row(TEAM_A, LeagueType.PUBLIC, 40, 20),  // win, difference +20, 40 points for
                row(TEAM_B, LeagueType.PUBLIC, 30, 10)); // win, difference +20, 30 points for

        List<LeaderboardAggregator.TeamTotals> totals =
                LeaderboardAggregator.aggregate(rows, LeaderboardAggregator.Scope.MASTER);
        totals.sort(LeaderboardAggregator.rankingOrder(LeaderboardAggregator.Scope.LEAGUE));

        assertEquals(TEAM_A, totals.get(0).getTeamId());
        assertEquals(TEAM_B, totals.get(1).getTeamId());
    }
}
