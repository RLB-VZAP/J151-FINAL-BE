package com.vzap.trytons.util.tournament;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KnockoutBracketTest {

    @Test
    @DisplayName("the bracket is the largest power of two within two per pool")
    void bracketSizeTakesTwoPerPoolRoundedDown() {
        // A full 100-manager league draws 25 pools of four.
        assertEquals(32, KnockoutBracket.bracketSize(25, 100));
        assertEquals(4, KnockoutBracket.bracketSize(3, 12));
        assertEquals(4, KnockoutBracket.bracketSize(2, 8));
        assertEquals(2, KnockoutBracket.bracketSize(1, 4));
        // A two-manager league still plays a final.
        assertEquals(2, KnockoutBracket.bracketSize(1, 2));
    }

    @Test
    @DisplayName("the bracket never asks for more managers than the league has")
    void bracketNeverExceedsLeagueSize() {
        for (int managerCount = 2; managerCount <= PoolAllocator.MAX_MANAGERS; managerCount++) {
            int poolCount = PoolAllocator.poolSizes(managerCount).size();
            int bracketSize = KnockoutBracket.bracketSize(poolCount, managerCount);

            assertTrue(bracketSize <= managerCount,
                    managerCount + " managers cannot fill a bracket of " + bracketSize);
            assertTrue(bracketSize >= 2, "bracket must hold at least a final");
            assertEquals(1, Integer.bitCount(bracketSize),
                    "bracket of " + bracketSize + " is not a power of two");
        }
    }

    @Test
    @DisplayName("a full league runs three pool rounds then five knockout rounds")
    void fullLeagueSeasonLength() {
        int poolCount = PoolAllocator.poolSizes(100).size();
        int bracketSize = KnockoutBracket.bracketSize(poolCount, 100);

        assertEquals(32, bracketSize);
        assertEquals(5, KnockoutBracket.knockoutRounds(bracketSize));
        // Three pool matchdays plus five knockout rounds is eight fantasy rounds.
        assertEquals(3, RoundRobinScheduler.matchdaysFor(4));
    }

    @Test
    @DisplayName("knockout round counts follow the bracket size")
    void knockoutRoundCounts() {
        assertEquals(1, KnockoutBracket.knockoutRounds(2));
        assertEquals(2, KnockoutBracket.knockoutRounds(4));
        assertEquals(3, KnockoutBracket.knockoutRounds(8));
        assertEquals(5, KnockoutBracket.knockoutRounds(32));
        assertThrows(IllegalArgumentException.class, () -> KnockoutBracket.knockoutRounds(6));
        assertThrows(IllegalArgumentException.class, () -> KnockoutBracket.knockoutRounds(1));
    }

    @Test
    @DisplayName("seed order is the standard reflected bracket")
    void seedOrderIsStandard() {
        assertEquals(List.of(1, 2), KnockoutBracket.seedOrder(2));
        assertEquals(List.of(1, 4, 2, 3), KnockoutBracket.seedOrder(4));
        assertEquals(List.of(1, 8, 4, 5, 2, 7, 3, 6), KnockoutBracket.seedOrder(8));
    }

    @Test
    @DisplayName("seed order lists every seed exactly once")
    void seedOrderIsAPermutation() {
        for (int bracketSize : new int[]{2, 4, 8, 16, 32}) {
            List<Integer> order = KnockoutBracket.seedOrder(bracketSize);
            assertEquals(bracketSize, order.size());
            Set<Integer> unique = new HashSet<>(order);
            assertEquals(bracketSize, unique.size(), "seed order repeats a seed");
            assertEquals(1, order.stream().mapToInt(Integer::intValue).min().orElseThrow());
            assertEquals(bracketSize, order.stream().mapToInt(Integer::intValue).max().orElseThrow());
        }
    }

    @Test
    @DisplayName("the top seed meets the bottom seed, and the top two can only meet in the final")
    void firstRoundPairsStrongestAgainstWeakest() {
        List<String> qualifiers = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            qualifiers.add("Q" + i);
        }

        List<RoundRobinScheduler.Pairing<String>> firstRound = KnockoutBracket.firstRound(qualifiers);

        assertEquals(4, firstRound.size());
        assertEquals("Q1", firstRound.get(0).homeTeamId());
        assertEquals("Q8", firstRound.get(0).awayTeamId());

        // Seeds one and two must sit in opposite halves of the bracket.
        int topSeedSlot = -1;
        int secondSeedSlot = -1;
        for (int slot = 0; slot < firstRound.size(); slot++) {
            RoundRobinScheduler.Pairing<String> pairing = firstRound.get(slot);
            if (pairing.homeTeamId().equals("Q1") || pairing.awayTeamId().equals("Q1")) {
                topSeedSlot = slot;
            }
            if (pairing.homeTeamId().equals("Q2") || pairing.awayTeamId().equals("Q2")) {
                secondSeedSlot = slot;
            }
        }
        assertTrue(topSeedSlot < 2, "top seed should be in the first half of the bracket");
        assertTrue(secondSeedSlot >= 2, "second seed should be in the second half of the bracket");
    }

    @Test
    @DisplayName("winners of adjacent slots meet in the next round")
    void nextRoundPairsAdjacentSlots() {
        List<RoundRobinScheduler.Pairing<String>> next =
                KnockoutBracket.nextRound(List.of("W0", "W1", "W2", "W3"));

        assertEquals(2, next.size());
        assertEquals("W0", next.get(0).homeTeamId());
        assertEquals("W1", next.get(0).awayTeamId());
        assertEquals("W2", next.get(1).homeTeamId());
        assertEquals("W3", next.get(1).awayTeamId());
    }

    @Test
    @DisplayName("an incomplete set of winners cannot form a round")
    void rejectsOddWinnerCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> KnockoutBracket.nextRound(List.of("W0", "W1", "W2")));
        assertThrows(IllegalArgumentException.class,
                () -> KnockoutBracket.nextRound(List.of("W0")));
    }
}
