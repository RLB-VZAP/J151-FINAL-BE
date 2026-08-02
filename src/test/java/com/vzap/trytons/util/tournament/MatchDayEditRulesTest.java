package com.vzap.trytons.util.tournament;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The two rules a hand-edited match day has to satisfy beyond
 * "is it a Wednesday, Saturday or Sunday" -- which is
 * {@link MatchdayCalendar#isMatchDay} and tested with it.
 */
class MatchDayEditRulesTest {

    // 2026-08-05 is a Wednesday, 2026-08-08 a Saturday, 2026-08-09 a Sunday.
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 8, 5);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 8, 8);

    // ---- isInFuture ---------------------------------------------------

    @Test
    @DisplayName("A match day later in the week is in the future")
    void laterDayIsInFuture() {
        assertTrue(MatchDayEditRules.isInFuture(SATURDAY, WEDNESDAY.atTime(16, 0)));
    }

    @Test
    @DisplayName("A match day already played is not in the future")
    void pastDayIsNotInFuture() {
        assertFalse(MatchDayEditRules.isInFuture(WEDNESDAY, SATURDAY.atTime(9, 0)));
    }

    @Test
    @DisplayName("Today counts while the 15:00 kickoff is still ahead")
    void todayBeforeKickoffIsInFuture() {
        assertTrue(MatchDayEditRules.isInFuture(WEDNESDAY, WEDNESDAY.atTime(14, 59)));
    }

    @Test
    @DisplayName("Kickoff itself is not in the future -- strictly after, so a round cannot be scheduled onto a kickoff that has arrived")
    void kickoffInstantIsNotInFuture() {
        assertFalse(MatchDayEditRules.isInFuture(WEDNESDAY, WEDNESDAY.atTime(15, 0)));
    }

    @Test
    @DisplayName("Today counts as past once the kickoff has gone")
    void todayAfterKickoffIsNotInFuture() {
        assertFalse(MatchDayEditRules.isInFuture(WEDNESDAY, WEDNESDAY.atTime(15, 1)));
    }

    @Test
    @DisplayName("Nulls are rejected rather than throwing")
    void nullsAreNotInFuture() {
        assertFalse(MatchDayEditRules.isInFuture(null, WEDNESDAY.atTime(9, 0)));
        assertFalse(MatchDayEditRules.isInFuture(WEDNESDAY, null));
    }

    // ---- fitsBetween --------------------------------------------------

    @Test
    @DisplayName("A kickoff between the two neighbours fits")
    void betweenNeighboursFits() {
        assertTrue(MatchDayEditRules.fitsBetween(
                SATURDAY.atTime(15, 0),
                WEDNESDAY.atTime(23, 59, 59),
                LocalDate.of(2026, 8, 12).atTime(0, 0)));
    }

    @Test
    @DisplayName("A kickoff on or before the previous matchday's close does not fit")
    void beforePreviousDoesNotFit() {
        LocalDateTime previousEnd = SATURDAY.atTime(23, 59, 59);
        assertFalse(MatchDayEditRules.fitsBetween(WEDNESDAY.atTime(15, 0), previousEnd, null));
        assertFalse(MatchDayEditRules.fitsBetween(previousEnd, previousEnd, null));
    }

    @Test
    @DisplayName("A kickoff on or after the next matchday's opening does not fit")
    void afterNextDoesNotFit() {
        LocalDateTime nextOpen = WEDNESDAY.atTime(0, 0);
        assertFalse(MatchDayEditRules.fitsBetween(SATURDAY.atTime(15, 0), null, nextOpen));
        assertFalse(MatchDayEditRules.fitsBetween(nextOpen, null, nextOpen));
    }

    @Test
    @DisplayName("A missing neighbour skips that half of the check rather than failing it")
    void missingNeighboursAreSkipped() {
        assertTrue(MatchDayEditRules.fitsBetween(SATURDAY.atTime(15, 0), null, null));
    }

    @Test
    @DisplayName("A null kickoff never fits")
    void nullKickoffDoesNotFit() {
        assertFalse(MatchDayEditRules.fitsBetween(null, null, null));
    }

    // ---- the two together ---------------------------------------------

    @Test
    @DisplayName("The generator's own windows satisfy both rules, so a round can be moved onto the day after its own")
    void generatedScheduleSatisfiesTheEditRules() {
        LocalDateTime anchor = WEDNESDAY.atTime(9, 0);
        var windows = MatchdayCalendar.schedule(anchor, 3);

        // Window 2 sits strictly between window 1's close and window 3's open,
        // which is exactly what the editor demands of a hand-picked day.
        assertTrue(MatchDayEditRules.fitsBetween(
                windows.get(1).lockDeadline(),
                windows.get(0).endDate(),
                windows.get(2).openDate()));
        assertTrue(MatchDayEditRules.isInFuture(windows.get(1).matchDay(), anchor));
    }
}
