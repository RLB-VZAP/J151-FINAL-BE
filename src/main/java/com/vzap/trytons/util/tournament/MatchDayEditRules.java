package com.vzap.trytons.util.tournament;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The rules a hand-edited match day has to satisfy, kept pure so they can be
 * tested without a database -- the same reason {@link MatchdayCalendar} and
 * {@link PoolAllocator} are pure.
 *
 * <p>{@link MatchdayCalendar} already answers "is this a legal match day at
 * all". What is left is the part that depends on the round's neighbours: a
 * manager may move their own round, but they may not reorder their own
 * competition by dragging matchday three in front of matchday two.
 */
public final class MatchDayEditRules {

    private MatchDayEditRules() {
    }

    /**
     * True when a round played on {@code matchDay} would still kick off in the
     * future. Strictly after, so "today at 15:00" is only movable before 15:00
     * -- a round cannot be scheduled for a kickoff that has already happened.
     */
    public static boolean isInFuture(LocalDate matchDay, LocalDateTime now) {
        if (matchDay == null || now == null) {
            return false;
        }
        return matchDay.atTime(MatchdayCalendar.KICKOFF).isAfter(now);
    }

    /**
     * True when a kickoff sits strictly between the round before it and the
     * round after it, so the tournament keeps the order it was drawn in.
     *
     * <p>A null neighbour means there is nothing on that side -- the first
     * matchday has no predecessor, and the last (the usual case, since pool
     * matchdays are minted one at a time) has no successor -- and that half of
     * the check is simply skipped rather than treated as a failure.
     *
     * @param previousEnd the previous matchday's {@code endDate}
     * @param nextOpen    the next matchday's {@code openDate}
     */
    public static boolean fitsBetween(LocalDateTime kickoff,
                                      LocalDateTime previousEnd,
                                      LocalDateTime nextOpen) {
        if (kickoff == null) {
            return false;
        }
        if (previousEnd != null && !kickoff.isAfter(previousEnd)) {
            return false;
        }
        return nextOpen == null || kickoff.isBefore(nextOpen);
    }
}
