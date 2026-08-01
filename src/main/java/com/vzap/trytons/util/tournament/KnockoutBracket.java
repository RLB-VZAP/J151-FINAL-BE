package com.vzap.trytons.util.tournament;

import java.util.ArrayList;
import java.util.List;

/**
 * Sizes and seeds the single-elimination stage that follows the pools.
 *
 * <p>Two managers qualify per pool where the bracket allows, exactly as the
 * Rugby World Cup does. Because a bracket must be a power of two, the size is
 * the largest power of two not exceeding twice the pool count: a 25-pool
 * league of 100 managers therefore plays a round of 32, taking all 25 pool
 * winners plus the seven best runners-up.
 *
 * <p>Losing a knockout fixture eliminates a manager outright. There is no
 * losers' bracket; only the optional third-place playoff gives a beaten
 * semi-finalist another fixture.
 */
public final class KnockoutBracket {

    private KnockoutBracket() {
    }

    /**
     * Size of the knockout bracket for a tournament.
     *
     * @param poolCount    number of pools drawn
     * @param managerCount total managers in the tournament
     */
    public static int bracketSize(int poolCount, int managerCount) {
        if (poolCount < 1) {
            throw new IllegalArgumentException("A tournament needs at least one pool");
        }
        if (managerCount < PoolAllocator.MIN_MANAGERS) {
            throw new IllegalArgumentException(
                    "A tournament needs at least " + PoolAllocator.MIN_MANAGERS + " managers");
        }

        int target = Math.min(poolCount * 2, managerCount);

        int size = 1;
        while (size * 2 <= target) {
            size *= 2;
        }
        // Even a two-manager league plays a final.
        return Math.max(size, 2);
    }

    /**
     * Number of knockout rounds, and therefore fantasy rounds, a bracket needs.
     */
    public static int knockoutRounds(int bracketSize) {
        if (bracketSize < 2 || Integer.bitCount(bracketSize) != 1) {
            throw new IllegalArgumentException("Bracket size must be a power of two of at least two");
        }
        return Integer.numberOfTrailingZeros(bracketSize);
    }

    /**
     * Standard bracket seeding order, one-based.
     *
     * <p>Built by reflecting each round: {@code [1,2]} becomes
     * {@code [1,4,2,3]} becomes {@code [1,8,4,5,2,7,3,6]}. Reading the result
     * in consecutive pairs gives the first-round fixtures, and the ordering
     * guarantees the top two seeds can only meet in the final.
     */
    public static List<Integer> seedOrder(int bracketSize) {
        if (bracketSize < 2 || Integer.bitCount(bracketSize) != 1) {
            throw new IllegalArgumentException("Bracket size must be a power of two of at least two");
        }

        List<Integer> order = new ArrayList<>(List.of(1));
        while (order.size() < bracketSize) {
            int reflection = (order.size() * 2) + 1;
            List<Integer> expanded = new ArrayList<>();
            for (int seed : order) {
                expanded.add(seed);
                expanded.add(reflection - seed);
            }
            order = expanded;
        }
        return List.copyOf(order);
    }

    /**
     * First-round pairings for an ordered list of qualifiers, strongest first.
     * Index in the returned list is the bracket slot; the winners of slots
     * {@code 2n} and {@code 2n + 1} meet in slot {@code n} of the next round.
     */
    public static <T> List<RoundRobinScheduler.Pairing<T>> firstRound(List<T> qualifiersBySeed) {
        int bracketSize = qualifiersBySeed.size();
        List<Integer> order = seedOrder(bracketSize);

        List<RoundRobinScheduler.Pairing<T>> pairings = new ArrayList<>();
        for (int i = 0; i < order.size(); i += 2) {
            T home = qualifiersBySeed.get(order.get(i) - 1);
            T away = qualifiersBySeed.get(order.get(i + 1) - 1);
            pairings.add(new RoundRobinScheduler.Pairing<>(home, away));
        }
        return List.copyOf(pairings);
    }

    /**
     * Pairs the winners of one knockout round into the next.
     *
     * @param winnersBySlot winners ordered by the bracket slot they came from
     */
    public static <T> List<RoundRobinScheduler.Pairing<T>> nextRound(List<T> winnersBySlot) {
        if (winnersBySlot.size() < 2 || winnersBySlot.size() % 2 != 0) {
            throw new IllegalArgumentException(
                    "A knockout round needs an even number of at least two winners");
        }

        List<RoundRobinScheduler.Pairing<T>> pairings = new ArrayList<>();
        for (int i = 0; i < winnersBySlot.size(); i += 2) {
            pairings.add(new RoundRobinScheduler.Pairing<>(winnersBySlot.get(i), winnersBySlot.get(i + 1)));
        }
        return List.copyOf(pairings);
    }
}
