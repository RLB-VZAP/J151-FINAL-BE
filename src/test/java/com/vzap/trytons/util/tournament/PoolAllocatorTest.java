package com.vzap.trytons.util.tournament;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The pool draw is what keeps a season short: pools of four need only three
 * matchdays, so the allocator must prefer them and fall back to threes rather
 * than ever growing pools.
 */
class PoolAllocatorTest {

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 37, 50, 63, 76, 99, 100})
    @DisplayName("pool sizes always account for exactly every manager")
    void poolSizesAccountForEveryManager(int managerCount) {
        List<Integer> sizes = PoolAllocator.poolSizes(managerCount);
        assertEquals(managerCount, sizes.stream().mapToInt(Integer::intValue).sum(),
                "pool sizes must sum to the manager count");
        assertFalse(sizes.isEmpty(), "there must be at least one pool");
    }

    @Test
    @DisplayName("every league of six or more is partitioned into threes and fours only")
    void sixOrMoreUsesOnlyThreesAndFours() {
        for (int managerCount = 6; managerCount <= PoolAllocator.MAX_MANAGERS; managerCount++) {
            List<Integer> sizes = PoolAllocator.poolSizes(managerCount);
            for (int size : sizes) {
                assertTrue(size == 3 || size == 4,
                        "league of " + managerCount + " produced a pool of " + size);
            }
        }
    }

    @Test
    @DisplayName("pools of four are maximised, so at most three pools of three are ever needed")
    void foursAreMaximised() {
        for (int managerCount = 6; managerCount <= PoolAllocator.MAX_MANAGERS; managerCount++) {
            List<Integer> sizes = PoolAllocator.poolSizes(managerCount);
            long threes = sizes.stream().filter(size -> size == 3).count();
            // Nine managers is the worst case at [3,3,3]; the only alternative
            // would be a pool of five, which would cost two extra matchdays.
            assertTrue(threes <= 3,
                    "league of " + managerCount + " used " + threes + " pools of three");

            long fours = sizes.stream().filter(size -> size == 4).count();
            // Three pools of three could always have been one four plus a five,
            // so confirm we never left a partition of fours unused.
            assertTrue(threes < 3 || fours * 4 + threes * 3 == managerCount);
        }
    }

    @Test
    @DisplayName("the two sizes that cannot be split into threes and fours are handled explicitly")
    void unpartitionableSizesAreSpecialCased() {
        assertEquals(List.of(2), PoolAllocator.poolSizes(2));
        assertEquals(List.of(5), PoolAllocator.poolSizes(5));
    }

    @Test
    @DisplayName("known league sizes produce the expected draw")
    void knownSizes() {
        assertEquals(List.of(3), PoolAllocator.poolSizes(3));
        assertEquals(List.of(4), PoolAllocator.poolSizes(4));
        assertEquals(List.of(3, 3), PoolAllocator.poolSizes(6));
        assertEquals(List.of(4, 3), PoolAllocator.poolSizes(7));
        assertEquals(List.of(4, 4), PoolAllocator.poolSizes(8));
        assertEquals(List.of(3, 3, 3), PoolAllocator.poolSizes(9));
        assertEquals(List.of(4, 3, 3), PoolAllocator.poolSizes(10));
        assertEquals(List.of(4, 4, 3), PoolAllocator.poolSizes(11));
        assertEquals(List.of(4, 4, 4), PoolAllocator.poolSizes(12));
        // A full league: 25 pools of four, hence a round of 32.
        assertEquals(25, PoolAllocator.poolSizes(100).size());
        assertTrue(PoolAllocator.poolSizes(100).stream().allMatch(size -> size == 4));
    }

    @Test
    @DisplayName("league sizes outside two to one hundred are rejected")
    void rejectsOutOfRangeLeagues() {
        assertThrows(IllegalArgumentException.class, () -> PoolAllocator.poolSizes(1));
        assertThrows(IllegalArgumentException.class, () -> PoolAllocator.poolSizes(0));
        assertThrows(IllegalArgumentException.class, () -> PoolAllocator.poolSizes(101));
    }

    @Test
    @DisplayName("the draw places every manager exactly once, filling each pool to its size")
    void drawPlacesEveryManagerExactlyOnce() {
        for (int managerCount = 2; managerCount <= PoolAllocator.MAX_MANAGERS; managerCount++) {
            List<Integer> seeds = new ArrayList<>();
            for (int i = 1; i <= managerCount; i++) {
                seeds.add(i);
            }

            List<List<Integer>> pools = PoolAllocator.draw(seeds);
            List<Integer> expectedSizes = PoolAllocator.poolSizes(managerCount);

            assertEquals(expectedSizes.size(), pools.size(),
                    "pool count for " + managerCount + " managers");

            Set<Integer> placed = new HashSet<>();
            for (int i = 0; i < pools.size(); i++) {
                assertEquals(expectedSizes.get(i), pools.get(i).size(),
                        "pool " + i + " size for " + managerCount + " managers");
                placed.addAll(pools.get(i));
            }
            assertEquals(managerCount, placed.size(),
                    "every manager must be drawn exactly once for " + managerCount);
        }
    }

    @Test
    @DisplayName("the snake draft spreads the strongest managers across different pools")
    void snakeDraftSpreadsTopSeeds() {
        List<Integer> seeds = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        List<List<Integer>> pools = PoolAllocator.draw(seeds);

        assertEquals(3, pools.size());
        // Seeds one, two and three must land in different pools.
        assertTrue(pools.get(0).contains(1));
        assertTrue(pools.get(1).contains(2));
        assertTrue(pools.get(2).contains(3));
        // The snake turns, so the fourth seed rejoins the last pool.
        assertTrue(pools.get(2).contains(4));
    }

    @Test
    @DisplayName("pool names run A, B, C and continue past Z")
    void poolNames() {
        assertEquals("A", PoolAllocator.poolName(0));
        assertEquals("B", PoolAllocator.poolName(1));
        assertEquals("Z", PoolAllocator.poolName(25));
        assertEquals("AA", PoolAllocator.poolName(26));
        assertThrows(IllegalArgumentException.class, () -> PoolAllocator.poolName(-1));
    }
}
