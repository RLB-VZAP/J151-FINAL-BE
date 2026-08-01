package com.vzap.trytons.util.tournament;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a single round robin so every manager in a pool meets every other
 * manager in that pool exactly once.
 *
 * <p>Uses the circle method: one manager is held fixed while the rest rotate,
 * which guarantees each manager appears at most once per matchday. That
 * property is what satisfies the database rule enforced by
 * {@code trg_fixture_integrity_insert} -- a fantasy team may only appear once
 * per league round -- and it is why one tournament matchday can map cleanly
 * onto one {@code fantasyRound}.
 *
 * <p>An odd pool gets a bye each matchday, so pools of three and four both
 * need exactly three matchdays.
 */
public final class RoundRobinScheduler {

    private RoundRobinScheduler() {
    }

    /**
     * One fixture within a pool.
     */
    public record Pairing<T>(T homeTeamId, T awayTeamId) {
    }

    /**
     * Number of matchdays a pool of {@code poolSize} managers needs.
     */
    public static int matchdaysFor(int poolSize) {
        if (poolSize < 2) {
            throw new IllegalArgumentException("A pool needs at least two managers");
        }
        return poolSize % 2 == 0 ? poolSize - 1 : poolSize;
    }

    /**
     * Round robin schedule for one pool, outer list indexed by matchday.
     * A matchday may hold fewer fixtures than others when the pool is odd,
     * because the manager drawn against the bye does not play.
     */
    public static <T> List<List<Pairing<T>>> schedule(List<T> teamIds) {
        if (teamIds.size() < 2) {
            throw new IllegalArgumentException("A pool needs at least two managers");
        }

        List<T> rotation = new ArrayList<>(teamIds);
        // A null entry is the bye that makes an odd pool schedulable.
        if (rotation.size() % 2 != 0) {
            rotation.add(null);
        }

        int slots = rotation.size();
        int matchdays = slots - 1;
        int fixturesPerMatchday = slots / 2;

        List<List<Pairing<T>>> schedule = new ArrayList<>();
        for (int matchday = 0; matchday < matchdays; matchday++) {
            List<Pairing<T>> fixtures = new ArrayList<>();

            for (int i = 0; i < fixturesPerMatchday; i++) {
                T home = rotation.get(i);
                T away = rotation.get(slots - 1 - i);
                if (home != null && away != null) {
                    // Alternate which side is nominally at home so a manager
                    // does not sit on the same side of every fixture.
                    if (matchday % 2 == 0) {
                        fixtures.add(new Pairing<>(home, away));
                    } else {
                        fixtures.add(new Pairing<>(away, home));
                    }
                }
            }
            schedule.add(List.copyOf(fixtures));

            // Hold the first slot fixed and rotate the remainder clockwise.
            T last = rotation.remove(slots - 1);
            rotation.add(1, last);
        }

        return List.copyOf(schedule);
    }
}
