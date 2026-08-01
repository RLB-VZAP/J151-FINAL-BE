package com.vzap.trytons.util.tournament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Draws managers into balanced Rugby World Cup style pools.
 *
 * <p>Pools of four are preferred because a four-pool round robin needs only
 * three matchdays; threes absorb whatever the fours cannot. Since every
 * manager count of six or more can be written as {@code 4b + 3a}, a pool of
 * five is only ever produced for a five-manager league, and a pool of two only
 * for a two-manager league. That is what keeps the pool stage at three
 * fantasy rounds for essentially every league size.
 *
 * <p>Pure and side-effect free so the draw can be unit tested without a
 * database, in the same spirit as {@code ScoringCalculator}.
 */
public final class PoolAllocator {

    public static final int MIN_MANAGERS = 2;
    public static final int MAX_MANAGERS = 100;

    private PoolAllocator() {
    }

    /**
     * Pool sizes for a league, largest first.
     *
     * @param managerCount number of managers, between {@value #MIN_MANAGERS}
     *                     and {@value #MAX_MANAGERS} inclusive
     */
    public static List<Integer> poolSizes(int managerCount) {
        if (managerCount < MIN_MANAGERS || managerCount > MAX_MANAGERS) {
            throw new IllegalArgumentException(
                    "A tournament needs between " + MIN_MANAGERS + " and " + MAX_MANAGERS
                            + " managers, but was given " + managerCount);
        }

        // Sizes that cannot be partitioned into threes and fours at all.
        if (managerCount == 2 || managerCount == 5) {
            return List.of(managerCount);
        }

        // Maximise pools of four, letting threes absorb the remainder.
        for (int fours = managerCount / 4; fours >= 0; fours--) {
            int remainder = managerCount - (fours * 4);
            if (remainder % 3 == 0) {
                List<Integer> sizes = new ArrayList<>();
                for (int i = 0; i < fours; i++) {
                    sizes.add(4);
                }
                for (int i = 0; i < remainder / 3; i++) {
                    sizes.add(3);
                }
                return List.copyOf(sizes);
            }
        }

        // Unreachable: every n >= 6 is expressible as 4b + 3a.
        throw new IllegalStateException("Cannot partition " + managerCount + " managers into pools");
    }

    /**
     * Distributes seeded managers across pools by snake draft, so the
     * strongest managers are spread evenly rather than concentrated.
     *
     * @param seededTeamIds manager identifiers, strongest first
     * @return one list of manager identifiers per pool, indexed as
     * {@link #poolSizes(int)}
     */
    public static <T> List<List<T>> draw(List<T> seededTeamIds) {
        List<Integer> sizes = poolSizes(seededTeamIds.size());

        List<List<T>> pools = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            pools.add(new ArrayList<>());
        }

        int assigned = 0;
        boolean forward = true;
        while (assigned < seededTeamIds.size()) {
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < pools.size(); i++) {
                order.add(i);
            }
            if (!forward) {
                Collections.reverse(order);
            }

            for (int poolIndex : order) {
                if (assigned >= seededTeamIds.size()) {
                    break;
                }
                if (pools.get(poolIndex).size() < sizes.get(poolIndex)) {
                    pools.get(poolIndex).add(seededTeamIds.get(assigned++));
                }
            }
            forward = !forward;
        }

        return pools;
    }

    /**
     * Pool label as shown to managers: A, B, ... Z, AA, AB, ...
     */
    public static String poolName(int poolIndex) {
        if (poolIndex < 0) {
            throw new IllegalArgumentException("Pool index cannot be negative");
        }
        StringBuilder name = new StringBuilder();
        int remaining = poolIndex;
        do {
            name.insert(0, (char) ('A' + (remaining % 26)));
            remaining = (remaining / 26) - 1;
        } while (remaining >= 0);
        return name.toString();
    }
}
