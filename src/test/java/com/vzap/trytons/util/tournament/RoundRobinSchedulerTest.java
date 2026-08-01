package com.vzap.trytons.util.tournament;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The round robin has to satisfy a database rule as well as a sporting one:
 * {@code trg_fixture_integrity_insert} rejects a fantasy team appearing twice
 * in the same league round, so no manager may be scheduled twice on a matchday.
 */
class RoundRobinSchedulerTest {

    private static List<String> managers(int count) {
        List<String> managers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            managers.add("M" + i);
        }
        return managers;
    }

    @Test
    @DisplayName("pools of three and four both take exactly three matchdays")
    void poolsOfThreeAndFourTakeThreeMatchdays() {
        assertEquals(3, RoundRobinScheduler.matchdaysFor(3));
        assertEquals(3, RoundRobinScheduler.matchdaysFor(4));
        // The sizes only reachable in very small leagues.
        assertEquals(1, RoundRobinScheduler.matchdaysFor(2));
        assertEquals(5, RoundRobinScheduler.matchdaysFor(5));
    }

    @Test
    @DisplayName("every manager meets every other manager exactly once")
    void everyPairMeetsExactlyOnce() {
        for (int poolSize = 2; poolSize <= 5; poolSize++) {
            List<String> pool = managers(poolSize);
            List<List<RoundRobinScheduler.Pairing<String>>> schedule = RoundRobinScheduler.schedule(pool);

            Set<Set<String>> seen = new HashSet<>();
            int fixtures = 0;
            for (List<RoundRobinScheduler.Pairing<String>> matchday : schedule) {
                for (RoundRobinScheduler.Pairing<String> pairing : matchday) {
                    Set<String> pair = Set.of(pairing.homeTeamId(), pairing.awayTeamId());
                    assertTrue(seen.add(pair),
                            "pool of " + poolSize + " scheduled " + pair + " more than once");
                    fixtures++;
                }
            }

            int expected = poolSize * (poolSize - 1) / 2;
            assertEquals(expected, fixtures,
                    "pool of " + poolSize + " must play every combination once");
        }
    }

    @Test
    @DisplayName("no manager is scheduled twice on the same matchday")
    void noManagerPlaysTwiceOnAMatchday() {
        for (int poolSize = 2; poolSize <= 5; poolSize++) {
            List<List<RoundRobinScheduler.Pairing<String>>> schedule =
                    RoundRobinScheduler.schedule(managers(poolSize));

            for (List<RoundRobinScheduler.Pairing<String>> matchday : schedule) {
                Set<String> playing = new HashSet<>();
                for (RoundRobinScheduler.Pairing<String> pairing : matchday) {
                    assertTrue(playing.add(pairing.homeTeamId()),
                            "pool of " + poolSize + ": " + pairing.homeTeamId() + " plays twice on a matchday");
                    assertTrue(playing.add(pairing.awayTeamId()),
                            "pool of " + poolSize + ": " + pairing.awayTeamId() + " plays twice on a matchday");
                }
            }
        }
    }

    @Test
    @DisplayName("the schedule spans the expected number of matchdays")
    void matchdayCountMatches() {
        for (int poolSize = 2; poolSize <= 5; poolSize++) {
            assertEquals(RoundRobinScheduler.matchdaysFor(poolSize),
                    RoundRobinScheduler.schedule(managers(poolSize)).size(),
                    "matchday count for a pool of " + poolSize);
        }
    }

    @Test
    @DisplayName("an odd pool rests exactly one manager per matchday")
    void oddPoolGivesOneByePerMatchday() {
        List<List<RoundRobinScheduler.Pairing<String>>> schedule =
                RoundRobinScheduler.schedule(managers(3));

        for (List<RoundRobinScheduler.Pairing<String>> matchday : schedule) {
            assertEquals(1, matchday.size(), "a pool of three plays one fixture per matchday");
        }
    }

    @Test
    @DisplayName("a pool needs at least two managers")
    void rejectsUndersizedPool() {
        assertThrows(IllegalArgumentException.class, () -> RoundRobinScheduler.schedule(List.of("M1")));
        assertThrows(IllegalArgumentException.class, () -> RoundRobinScheduler.matchdaysFor(1));
    }
}
