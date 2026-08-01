package com.vzap.trytons.util.tournament;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes pool standings using Rugby World Cup scoring, adapted to fantasy
 * points.
 *
 * <p>Four points for a win, two for a draw, none for a loss, plus two bonus
 * points carried over from the real competition: the try-scoring bonus becomes
 * a fantasy-points threshold, and the losing bonus keeps its "beaten by a
 * narrow margin" meaning with the margin expressed in fantasy points.
 *
 * <p>Tie-breakers follow the World Cup order, substituting fantasy statistics
 * for match statistics: tournament points, then the head-to-head result, then
 * fantasy points difference, then fantasy points scored, then matches won, and
 * finally the draw seed so ordering is always deterministic.
 *
 * <p>Pure and side-effect free, so the whole ranking rule set is unit testable
 * without a database.
 */
public final class PoolStandingsCalculator {

    private PoolStandingsCalculator() {
    }

    /**
     * Tunable half of the scoring rules, mirroring {@code tournament_settings}.
     */
    public record Rules(int winPoints,
                        int drawPoints,
                        int lossPoints,
                        int attackBonusThreshold,
                        int losingBonusMargin) {

        /**
         * Rugby World Cup scoring, with the bonus points calibrated against
         * observed fantasy scoring rather than borrowed unchanged.
         *
         * <p>A simulated fantasy fixture scores far higher than a rugby match:
         * a sample of simulated fixtures ran from 132 to 216 points, averaging
         * 170. A literal 4-try threshold of 75 was therefore met in every
         * single innings, which made the attacking bonus a flat +1 for
         * everyone. 190 is met roughly a quarter of the time, which is about
         * how often the four-try bonus is actually earned in the real
         * competition.
         *
         * <p>The losing margin stays at 7. Fantasy margins are wide on
         * average, but close finishes still cluster tightly, and 7 was earned
         * in about a fifth of defeats -- close to the real rate, and it keeps
         * the direct parallel with the rugby laws.
         */
        public static Rules rugbyWorldCupDefaults() {
            return new Rules(4, 2, 0, 190, 7);
        }
    }

    /**
     * One completed pool fixture, seen from one manager's side.
     */
    public record Outcome<T>(T teamId, T opponentId, int pointsFor, int pointsAgainst) {
    }

    /**
     * A manager's computed row in the pool table.
     */
    public static final class Standing<T> {
        private final T teamId;
        private final int seed;

        private int played;
        private int won;
        private int drawn;
        private int lost;
        private int pointsFor;
        private int pointsAgainst;
        private int attackBonus;
        private int losingBonus;
        private int tournamentPoints;
        private int position;

        private Standing(T teamId, int seed) {
            this.teamId = teamId;
            this.seed = seed;
        }

        public T getTeamId() {
            return teamId;
        }

        public int getSeed() {
            return seed;
        }

        public int getPlayed() {
            return played;
        }

        public int getWon() {
            return won;
        }

        public int getDrawn() {
            return drawn;
        }

        public int getLost() {
            return lost;
        }

        public int getPointsFor() {
            return pointsFor;
        }

        public int getPointsAgainst() {
            return pointsAgainst;
        }

        public int getPointsDifference() {
            return pointsFor - pointsAgainst;
        }

        public int getAttackBonus() {
            return attackBonus;
        }

        public int getLosingBonus() {
            return losingBonus;
        }

        public int getTournamentPoints() {
            return tournamentPoints;
        }

        /** One-based finishing position within the pool. */
        public int getPosition() {
            return position;
        }
    }

    /**
     * Builds the ordered pool table.
     *
     * @param seedsByTeam every manager in the pool mapped to their draw seed;
     *                    managers with no completed fixtures still appear
     * @param outcomes    completed fixtures, supplied once per side
     * @param rules       scoring configuration
     * @return standings ordered best first, with positions assigned
     */
    public static <T> List<Standing<T>> calculate(Map<T, Integer> seedsByTeam,
                                                  List<Outcome<T>> outcomes,
                                                  Rules rules) {
        Map<T, Standing<T>> table = new LinkedHashMap<>();
        seedsByTeam.forEach((teamId, seed) -> table.put(teamId, new Standing<>(teamId, seed)));

        // Head-to-head lookup, used only to separate exactly two tied managers.
        Map<T, Map<T, Integer>> headToHead = new HashMap<>();

        for (Outcome<T> outcome : outcomes) {
            Standing<T> standing = table.get(outcome.teamId());
            if (standing == null) {
                throw new IllegalArgumentException(
                        "Outcome references a manager who is not in this pool: " + outcome.teamId());
            }

            standing.played++;
            standing.pointsFor += outcome.pointsFor();
            standing.pointsAgainst += outcome.pointsAgainst();

            int margin = outcome.pointsFor() - outcome.pointsAgainst();
            if (margin > 0) {
                standing.won++;
                standing.tournamentPoints += rules.winPoints();
            } else if (margin == 0) {
                standing.drawn++;
                standing.tournamentPoints += rules.drawPoints();
            } else {
                standing.lost++;
                standing.tournamentPoints += rules.lossPoints();
                // Losing bonus: beaten inside the configured fantasy margin.
                if (-margin <= rules.losingBonusMargin()) {
                    standing.losingBonus++;
                    standing.tournamentPoints++;
                }
            }

            // Attacking bonus: the fantasy equivalent of scoring four tries.
            if (outcome.pointsFor() >= rules.attackBonusThreshold()) {
                standing.attackBonus++;
                standing.tournamentPoints++;
            }

            headToHead.computeIfAbsent(outcome.teamId(), key -> new HashMap<>())
                    .merge(outcome.opponentId(), Integer.signum(margin), Integer::sum);
        }

        List<Standing<T>> ordered = new ArrayList<>(table.values());
        ordered.sort(Comparator
                .comparingInt((Standing<T> standing) -> standing.tournamentPoints).reversed()
                .thenComparing(Comparator.comparingInt(Standing<T>::getPointsDifference).reversed())
                .thenComparing(Comparator.comparingInt(Standing<T>::getPointsFor).reversed())
                .thenComparing(Comparator.comparingInt(Standing<T>::getWon).reversed())
                .thenComparingInt(Standing::getSeed));

        applyHeadToHead(ordered, headToHead);

        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).position = i + 1;
        }
        return ordered;
    }

    /**
     * World Cup rules only use the head-to-head result to separate two managers
     * level on points; with three or more level it is skipped in favour of
     * points difference, which the primary sort has already applied.
     */
    private static <T> void applyHeadToHead(List<Standing<T>> ordered,
                                            Map<T, Map<T, Integer>> headToHead) {
        int index = 0;
        while (index < ordered.size()) {
            int end = index;
            while (end + 1 < ordered.size()
                    && ordered.get(end + 1).tournamentPoints == ordered.get(index).tournamentPoints) {
                end++;
            }

            if (end - index == 1) {
                Standing<T> upper = ordered.get(index);
                Standing<T> lower = ordered.get(end);
                int result = headToHead
                        .getOrDefault(upper.teamId, Map.of())
                        .getOrDefault(lower.teamId, 0);
                if (result < 0) {
                    ordered.set(index, lower);
                    ordered.set(end, upper);
                }
            }

            index = end + 1;
        }
    }
}
