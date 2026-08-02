package com.vzap.trytons.util.tournament;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fixture scheduling used to pick whichever fantasy round had the lowest round
 * number and was still UPCOMING, with no check that the date was in the
 * future: a league started on 2026-08-02 was handed a fixture dated
 * 2026-07-24, nine days before its own league existed.
 *
 * <p>These tests pin the replacement. The rules being protected are:
 * <ul>
 *   <li>a matchday is only ever a Monday, Wednesday, Friday, Saturday or
 *       Sunday -- never a Tuesday or a Thursday;</li>
 *   <li>a window's kickoff is strictly after the anchor it was scheduled
 *       from -- never on it, never before it;</li>
 *   <li>every window satisfies both halves of {@code chk_fantasyRound_dates},
 *       which the database enforces and which no unit test elsewhere can
 *       see.</li>
 * </ul>
 */
class MatchdayCalendarTest {

    // 2026-08-03 is a Monday, which fixes the weekday of every date below.
    private static final LocalDate MONDAY = LocalDate.of(2026, 8, 3);
    private static final LocalDate TUESDAY = LocalDate.of(2026, 8, 4);
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 8, 5);
    private static final LocalDate THURSDAY = LocalDate.of(2026, 8, 6);
    private static final LocalDate FRIDAY = LocalDate.of(2026, 8, 7);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 8, 8);
    private static final LocalDate SUNDAY = LocalDate.of(2026, 8, 9);

    @Test
    @DisplayName("Match days are Mon, Wed, Fri, Sat, Sun -- and only those")
    void matchDaysAreTheRugbyWeek() {
        assertEquals(DayOfWeek.MONDAY, MONDAY.getDayOfWeek(), "test fixture dates have drifted");

        assertTrue(MatchdayCalendar.isMatchDay(MONDAY));
        assertTrue(MatchdayCalendar.isMatchDay(WEDNESDAY));
        assertTrue(MatchdayCalendar.isMatchDay(FRIDAY));
        assertTrue(MatchdayCalendar.isMatchDay(SATURDAY));
        assertTrue(MatchdayCalendar.isMatchDay(SUNDAY));

        // The only two days that are not match days.
        assertFalse(MatchdayCalendar.isMatchDay(TUESDAY));
        assertFalse(MatchdayCalendar.isMatchDay(THURSDAY));
        assertFalse(MatchdayCalendar.isMatchDay(null));
    }

    @Test
    @DisplayName("A Monday morning anchor still plays that Monday")
    void mondayMorningKeepsTheSameDay() {
        assertEquals(MONDAY, MatchdayCalendar.firstMatchDayAfter(MONDAY.atTime(9, 0)));
    }

    @Test
    @DisplayName("A Tuesday anchor rolls on to the Wednesday")
    void tuesdayAnchorLandsOnWednesday() {
        assertEquals(WEDNESDAY, MatchdayCalendar.firstMatchDayAfter(TUESDAY.atTime(9, 0)));
    }

    @Test
    @DisplayName("A Thursday anchor rolls on to the Friday")
    void thursdayAnchorLandsOnFriday() {
        assertEquals(FRIDAY, MatchdayCalendar.firstMatchDayAfter(THURSDAY.atTime(9, 0)));
    }

    @Test
    @DisplayName("A Wednesday morning anchor still plays that Wednesday")
    void wednesdayMorningKeepsTheSameDay() {
        assertEquals(WEDNESDAY, MatchdayCalendar.firstMatchDayAfter(WEDNESDAY.atTime(9, 0)));
    }

    @Test
    @DisplayName("A Wednesday anchor after kickoff rolls on to the Friday")
    void wednesdayAfterKickoffRollsToFriday() {
        assertEquals(FRIDAY, MatchdayCalendar.firstMatchDayAfter(WEDNESDAY.atTime(16, 0)));
    }

    @Test
    @DisplayName("Kickoff exactly on the anchor is not strictly after it")
    void kickoffOnTheAnchorRollsForward() {
        assertEquals(FRIDAY,
                MatchdayCalendar.firstMatchDayAfter(WEDNESDAY.atTime(MatchdayCalendar.DEFAULT_KICKOFF)));
    }

    @Test
    @DisplayName("A Saturday morning anchor still plays that Saturday")
    void saturdayMorningKeepsTheSameDay() {
        assertEquals(SATURDAY, MatchdayCalendar.firstMatchDayAfter(SATURDAY.atTime(9, 0)));
    }

    @Test
    @DisplayName("The matchday after a Sunday is the following Monday")
    void sundayIsFollowedByMonday() {
        assertEquals(MONDAY.plusWeeks(1), MatchdayCalendar.nextMatchDayAfter(SUNDAY));
    }

    @Test
    @DisplayName("A Sunday evening anchor rolls on to the following Monday")
    void sundayEveningRollsToMonday() {
        assertEquals(MONDAY.plusWeeks(1), MatchdayCalendar.firstMatchDayAfter(SUNDAY.atTime(20, 0)));
    }

    @Test
    @DisplayName("Six windows follow Mon, Wed, Fri, Sat, Sun, Mon")
    void sixWindowsFollowTheRugbyWeek() {
        List<MatchdayCalendar.Window> windows = MatchdayCalendar.schedule(MONDAY.atTime(9, 0), 6);

        assertEquals(6, windows.size());
        assertEquals(List.of(
                        MONDAY,
                        WEDNESDAY,
                        FRIDAY,
                        SATURDAY,
                        SUNDAY,
                        MONDAY.plusWeeks(1)),
                windows.stream().map(MatchdayCalendar.Window::matchDay).toList());
    }

    @Test
    @DisplayName("Every window satisfies both halves of chk_fantasyRound_dates")
    void everyWindowSatisfiesTheDatabaseCheck() {
        List<MatchdayCalendar.Window> windows = MatchdayCalendar.schedule(MONDAY.atTime(9, 0), 12);

        assertEquals(12, windows.size());
        for (MatchdayCalendar.Window window : windows) {
            // lockDeadline >= openDate
            assertFalse(window.lockDeadline().isBefore(window.openDate()),
                    "lockDeadline before openDate for " + window.matchDay());
            // endDate >= lockDeadline
            assertFalse(window.endDate().isBefore(window.lockDeadline()),
                    "endDate before lockDeadline for " + window.matchDay());
        }
    }

    @Test
    @DisplayName("Windows are strictly increasing and never overlap")
    void windowsAreStrictlyIncreasingAndDisjoint() {
        List<MatchdayCalendar.Window> windows = MatchdayCalendar.schedule(MONDAY.atTime(9, 0), 12);

        for (int i = 1; i < windows.size(); i++) {
            MatchdayCalendar.Window previous = windows.get(i - 1);
            MatchdayCalendar.Window current = windows.get(i);

            assertTrue(current.matchDay().isAfter(previous.matchDay()),
                    "matchdays must strictly increase");
            assertTrue(current.lockDeadline().isAfter(previous.lockDeadline()),
                    "kickoffs must strictly increase");
            assertTrue(current.openDate().isAfter(previous.endDate()),
                    "a window must open after the previous one has closed");
        }
    }

    @Test
    @DisplayName("Every window kicks off at 15:00 on its own matchday")
    void lockDeadlineIsKickoffOnTheMatchday() {
        for (MatchdayCalendar.Window window : MatchdayCalendar.schedule(MONDAY.atTime(9, 0), 8)) {
            assertEquals(window.matchDay().atTime(MatchdayCalendar.DEFAULT_KICKOFF), window.lockDeadline());
            assertEquals(window.matchDay(), window.lockDeadline().toLocalDate());
            assertTrue(MatchdayCalendar.isMatchDay(window.matchDay()));
        }
    }

    @Test
    @DisplayName("The first window never kicks off before its anchor")
    void firstWindowIsStrictlyAfterTheAnchor() {
        LocalDateTime anchor = LocalDate.of(2026, 8, 2).atTime(11, 30); // a Sunday, before kickoff
        MatchdayCalendar.Window first = MatchdayCalendar.schedule(anchor, 3).get(0);

        assertTrue(first.lockDeadline().isAfter(anchor));
        assertEquals(anchor, first.openDate());
        assertEquals(LocalDate.of(2026, 8, 2), first.matchDay(),
                "11:30 on a Sunday is still before that Sunday's 15:00 kickoff");
    }

    @Test
    @DisplayName("A non-positive count schedules nothing")
    void nonPositiveCountSchedulesNothing() {
        assertTrue(MatchdayCalendar.schedule(MONDAY.atTime(9, 0), 0).isEmpty());
        assertTrue(MatchdayCalendar.schedule(MONDAY.atTime(9, 0), -3).isEmpty());
    }

    @Test
    @DisplayName("A chosen kickoff becomes the round's lock deadline")
    void windowForHonoursAChosenKickoff() {
        MatchdayCalendar.Window window =
                MatchdayCalendar.windowFor(SATURDAY, SATURDAY.atTime(8, 0), LocalTime.of(19, 30));

        // The kickoff IS the lock deadline: every fixture in the round reads its
        // date and time from this one timestamp, so the two cannot drift apart.
        assertEquals(SATURDAY.atTime(19, 30), window.lockDeadline());
        assertEquals(SATURDAY, window.matchDay());

        // chk_fantasyRound_dates still holds at the new time.
        assertFalse(window.lockDeadline().isBefore(window.openDate()));
        assertFalse(window.endDate().isBefore(window.lockDeadline()));
    }

    @Test
    @DisplayName("A late kickoff still clamps an opening that would follow it")
    void windowForClampsAnOpeningAfterAChosenKickoff() {
        MatchdayCalendar.Window window =
                MatchdayCalendar.windowFor(SATURDAY, SATURDAY.atTime(23, 0), LocalTime.of(11, 0));

        assertEquals(SATURDAY.atTime(11, 0), window.lockDeadline());
        assertEquals(SATURDAY.atTime(11, 0), window.openDate(), "an opening after kickoff must clamp to it");
        assertFalse(window.lockDeadline().isBefore(window.openDate()));
    }

    @Test
    @DisplayName("A null kickoff falls back to the default")
    void windowForFallsBackToTheDefaultKickoff() {
        MatchdayCalendar.Window window = MatchdayCalendar.windowFor(SATURDAY, SATURDAY.atTime(8, 0), null);
        assertEquals(SATURDAY.atTime(MatchdayCalendar.DEFAULT_KICKOFF), window.lockDeadline());
    }

    @Test
    @DisplayName("A run over the new year keeps its ordering")
    void aRunAcrossTheNewYearKeepsOrdering() {
        // 2026-12-28 is a Monday, so this run crosses into 2027. Monday is a
        // match day and 09:00 is before kickoff, so the run opens that same day:
        // Mon 28 Dec, Wed 30 Dec, Fri 1 Jan, Sat 2 Jan, Sun 3 Jan, Mon 4 Jan.
        List<MatchdayCalendar.Window> windows =
                MatchdayCalendar.schedule(LocalDate.of(2026, 12, 28).atTime(9, 0), 6);

        assertEquals(LocalDate.of(2026, 12, 28), windows.get(0).matchDay());
        assertEquals(LocalDate.of(2027, 1, 1), windows.get(2).matchDay());
        assertEquals(LocalDate.of(2027, 1, 4), windows.get(5).matchDay());

        for (int i = 1; i < windows.size(); i++) {
            assertTrue(windows.get(i).matchDay().isAfter(windows.get(i - 1).matchDay()),
                    "matchdays must increase across the year boundary");
            assertTrue(windows.get(i).openDate().isAfter(windows.get(i - 1).endDate()),
                    "windows must stay disjoint across the year boundary");
            assertFalse(windows.get(i).lockDeadline().isBefore(windows.get(i).openDate()));
            assertFalse(windows.get(i).endDate().isBefore(windows.get(i).lockDeadline()));
        }
    }

    @Test
    @DisplayName("windowFor clamps an opening that would fall after kickoff")
    void windowForClampsALateOpening() {
        MatchdayCalendar.Window window =
                MatchdayCalendar.windowFor(SATURDAY, SUNDAY.atTime(9, 0));

        assertEquals(SATURDAY.atTime(MatchdayCalendar.DEFAULT_KICKOFF), window.openDate());
        assertFalse(window.lockDeadline().isBefore(window.openDate()));
        assertFalse(window.endDate().isBefore(window.lockDeadline()));
    }

    @Test
    @DisplayName("windowFor keeps an opening that already precedes kickoff")
    void windowForKeepsAnEarlyOpening() {
        LocalDateTime opened = MONDAY.atTime(LocalTime.of(0, 0));
        MatchdayCalendar.Window window = MatchdayCalendar.windowFor(WEDNESDAY, opened);

        assertEquals(opened, window.openDate());
        assertEquals(WEDNESDAY.atTime(MatchdayCalendar.DEFAULT_KICKOFF), window.lockDeadline());
    }
}
