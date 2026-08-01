package com.vzap.trytons.util.tournament;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rugby World Cup pool scoring, with the try bonus re-expressed as a fantasy
 * points threshold and the losing bonus margin in fantasy points.
 */
class PoolStandingsCalculatorTest {

    /**
     * Explicit rules rather than the shipped defaults, so these tests pin the
     * scoring logic and stay readable at small scores. The defaults are
     * tunable and calibrated against real fantasy scoring; they are asserted
     * separately in {@link #shippedDefaultsAreCalibratedForFantasyScoring()}.
     */
    private static final PoolStandingsCalculator.Rules RULES =
            new PoolStandingsCalculator.Rules(4, 2, 0, 75, 7);

    /** Records both sides of a fixture, as the calculator expects. */
    private static void addFixture(List<PoolStandingsCalculator.Outcome<String>> outcomes,
                                   String home, int homeScore,
                                   String away, int awayScore) {
        outcomes.add(new PoolStandingsCalculator.Outcome<>(home, away, homeScore, awayScore));
        outcomes.add(new PoolStandingsCalculator.Outcome<>(away, home, awayScore, homeScore));
    }

    private static Map<String, Integer> seeds(String... teams) {
        Map<String, Integer> seeds = new LinkedHashMap<>();
        for (int i = 0; i < teams.length; i++) {
            seeds.put(teams[i], i + 1);
        }
        return seeds;
    }

    private static PoolStandingsCalculator.Standing<String> find(
            List<PoolStandingsCalculator.Standing<String>> table, String teamId) {
        return table.stream()
                .filter(standing -> standing.getTeamId().equals(teamId))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("shipped defaults keep the attacking bonus a genuine reward, not a flat bonus")
    void shippedDefaultsAreCalibratedForFantasyScoring() {
        PoolStandingsCalculator.Rules defaults =
                PoolStandingsCalculator.Rules.rugbyWorldCupDefaults();

        assertEquals(4, defaults.winPoints());
        assertEquals(2, defaults.drawPoints());
        assertEquals(0, defaults.lossPoints());
        assertEquals(7, defaults.losingBonusMargin());

        // Simulated fixtures score 132-216, averaging about 170. A threshold at
        // or below a typical score would award the attacking bonus in every
        // innings, which is exactly what a literal 75 did.
        assertTrue(defaults.attackBonusThreshold() > 170,
                "the attacking bonus must not be earned by an average score");

        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        addFixture(outcomes, "A", 170, "B", 140);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B"), outcomes, defaults);

        assertEquals(0, find(table, "A").getAttackBonus(),
                "an average winning score should not earn the attacking bonus");
        assertEquals(4, find(table, "A").getTournamentPoints());
    }

    @Test
    @DisplayName("a win scores four, a draw two and a loss none")
    void baseMatchPoints() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        // Scores kept under the attacking threshold and margins over the losing
        // bonus margin, so only the base points apply.
        addFixture(outcomes, "A", 50, "B", 20);
        addFixture(outcomes, "C", 30, "D", 30);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B", "C", "D"), outcomes, RULES);

        assertEquals(4, find(table, "A").getTournamentPoints());
        assertEquals(0, find(table, "B").getTournamentPoints());
        assertEquals(2, find(table, "C").getTournamentPoints());
        assertEquals(2, find(table, "D").getTournamentPoints());

        assertEquals(1, find(table, "A").getWon());
        assertEquals(1, find(table, "B").getLost());
        assertEquals(1, find(table, "C").getDrawn());
    }

    @Test
    @DisplayName("scoring at or above the threshold earns an attacking bonus")
    void attackingBonus() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        // A clears the 75 point threshold; B does not.
        addFixture(outcomes, "A", 80, "B", 20);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B"), outcomes, RULES);

        assertEquals(1, find(table, "A").getAttackBonus());
        assertEquals(5, find(table, "A").getTournamentPoints(), "four for the win plus one bonus");
        assertEquals(0, find(table, "B").getAttackBonus());
        assertEquals(0, find(table, "B").getTournamentPoints());
    }

    @Test
    @DisplayName("losing inside the margin earns a losing bonus")
    void losingBonus() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        // A seven point defeat is exactly on the boundary and still qualifies.
        addFixture(outcomes, "A", 40, "B", 33);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B"), outcomes, RULES);

        assertEquals(1, find(table, "B").getLosingBonus());
        assertEquals(1, find(table, "B").getTournamentPoints());
        assertEquals(4, find(table, "A").getTournamentPoints());
    }

    @Test
    @DisplayName("a defeat outside the margin earns nothing")
    void noLosingBonusBeyondMargin() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        addFixture(outcomes, "A", 40, "B", 32);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B"), outcomes, RULES);

        assertEquals(0, find(table, "B").getLosingBonus());
        assertEquals(0, find(table, "B").getTournamentPoints());
    }

    @Test
    @DisplayName("both bonuses can be earned in the same fixture")
    void bonusesStack() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        // B loses by five but still passes the attacking threshold.
        addFixture(outcomes, "A", 90, "B", 85);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B"), outcomes, RULES);

        assertEquals(2, find(table, "B").getTournamentPoints(),
                "an attacking bonus plus a losing bonus");
        assertEquals(1, find(table, "B").getAttackBonus());
        assertEquals(1, find(table, "B").getLosingBonus());
        assertEquals(5, find(table, "A").getTournamentPoints());
    }

    @Test
    @DisplayName("points difference separates managers level on points")
    void pointsDifferenceBreaksTies() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        // A and B each beat a different opponent; A wins by more.
        addFixture(outcomes, "A", 60, "C", 20);
        addFixture(outcomes, "B", 60, "D", 40);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B", "C", "D"), outcomes, RULES);

        assertEquals("A", table.get(0).getTeamId());
        assertEquals("B", table.get(1).getTeamId());
        assertEquals(40, table.get(0).getPointsDifference());
        assertEquals(20, table.get(1).getPointsDifference());
    }

    @Test
    @DisplayName("the head-to-head result outranks points difference for exactly two tied managers")
    void headToHeadBreaksTwoWayTies() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        // B beats A head to head by a margin too wide for a losing bonus, then
        // A builds a far better points difference against C. Both finish on
        // four points, so only the head-to-head result can separate them.
        addFixture(outcomes, "B", 30, "A", 20);
        addFixture(outcomes, "A", 60, "C", 10);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B", "C", "D"), outcomes, RULES);

        assertTrue(find(table, "A").getPointsDifference() > find(table, "B").getPointsDifference(),
                "A must hold the better difference for this test to prove anything");

        PoolStandingsCalculator.Standing<String> a = find(table, "A");
        PoolStandingsCalculator.Standing<String> b = find(table, "B");
        assertEquals(a.getTournamentPoints(), b.getTournamentPoints(),
                "the fixture is only meaningful if both are level on points");
        assertEquals("B", table.get(0).getTeamId(),
                "the head-to-head winner must be placed first");
    }

    @Test
    @DisplayName("managers who have not played yet still appear in the table")
    void unplayedManagersAppear() {
        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B", "C", "D"), List.of(), RULES);

        assertEquals(4, table.size());
        assertTrue(table.stream().allMatch(standing -> standing.getPlayed() == 0));
        assertTrue(table.stream().allMatch(standing -> standing.getTournamentPoints() == 0));
    }

    @Test
    @DisplayName("positions are assigned from one upwards and ordering is deterministic")
    void positionsAreAssigned() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        addFixture(outcomes, "A", 50, "B", 10);

        List<PoolStandingsCalculator.Standing<String>> table =
                PoolStandingsCalculator.calculate(seeds("A", "B", "C", "D"), outcomes, RULES);

        for (int i = 0; i < table.size(); i++) {
            assertEquals(i + 1, table.get(i).getPosition());
        }

        assertEquals("A", table.get(0).getTeamId(), "the only winner leads the pool");
        // C and D are level on nothing at all, so the draw seed separates them,
        // and both sit above B whose heavy defeat gives a negative difference.
        assertEquals("C", table.get(1).getTeamId());
        assertEquals("D", table.get(2).getTeamId());
        assertEquals("B", table.get(3).getTeamId());
    }

    @Test
    @DisplayName("an outcome for a manager outside the pool is rejected")
    void rejectsUnknownManager() {
        List<PoolStandingsCalculator.Outcome<String>> outcomes = new ArrayList<>();
        addFixture(outcomes, "A", 10, "Z", 5);

        assertThrows(IllegalArgumentException.class,
                () -> PoolStandingsCalculator.calculate(seeds("A", "B"), outcomes, RULES));
    }
}
