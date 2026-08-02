package com.vzap.trytons.util.tournament;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The one place that decides when a fantasy round is played.
 *
 * <p>Rugby is played midweek and at the weekend, so a matchday is a Monday,
 * Wednesday, Friday, Saturday or Sunday. Generated rounds kick off at
 * {@link #DEFAULT_KICKOFF}; a round's own kickoff can then be moved by whoever
 * runs the league. Every generated round and every hand-edited round is
 * measured against {@link #isMatchDay(LocalDate)}, so the generate path and the
 * edit path cannot disagree about what a legal matchday is.
 *
 * <p>Pure and side-effect free, in the same spirit as {@link PoolAllocator}
 * and {@link KnockoutBracket}: the test suite has no database, so all of the
 * date arithmetic lives here where it can actually be tested.
 *
 * <h2>Why the window is shaped the way it is</h2>
 * A {@code fantasyRound} row carries three timestamps and the database
 * enforces {@code chk_fantasyRound_dates}:
 * {@code lockDeadline >= openDate AND (endDate IS NULL OR endDate >= lockDeadline)}.
 * A {@link Window} is built so that check can never fail:
 * <ul>
 *   <li>{@code lockDeadline} is the matchday's kickoff -- squads lock when the
 *       rugby starts, and this timestamp is the single source of a fixture's
 *       date;</li>
 *   <li>{@code endDate} is the end of the same day, so it is always at or
 *       after the lock deadline;</li>
 *   <li>{@code openDate} is the moment the previous window closed (or the
 *       anchor, for the first window), so it is always before the next
 *       kickoff and consecutive transfer windows never overlap.</li>
 * </ul>
 */
public final class MatchdayCalendar {

    /** The days rugby is played on. */
    public static final Set<DayOfWeek> MATCH_DAYS = EnumSet.of(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY);

    /**
     * What a generated fixture kicks off at until somebody moves it. A round's
     * actual kickoff is whatever its {@code lockDeadline} says, which is why
     * every read of a fixture's time goes through the round rather than this
     * constant.
     */
    public static final LocalTime DEFAULT_KICKOFF = LocalTime.of(15, 0);

    /** The last instant of a matchday; {@code endDate} of that matchday's window. */
    private static final LocalTime DAY_END = LocalTime.of(23, 59, 59);

    private MatchdayCalendar() {
    }

    /**
     * The single matchday predicate. Both tournament generation and the
     * per-round match-day editor validate against this, so a date one accepts
     * the other can never reject.
     */
    public static boolean isMatchDay(LocalDate date) {
        return date != null && MATCH_DAYS.contains(date.getDayOfWeek());
    }

    /**
     * The first matchday whose kickoff falls strictly after {@code from}.
     *
     * <p>Strictly, so a league started at 16:00 on a Wednesday does not get a
     * fixture that kicked off an hour earlier -- the bug this class exists to
     * kill.
     */
    public static LocalDate firstMatchDayAfter(LocalDateTime from) {
        return firstMatchDayAfter(from, DEFAULT_KICKOFF);
    }

    /** As {@link #firstMatchDayAfter(LocalDateTime)}, for a chosen kickoff. */
    public static LocalDate firstMatchDayAfter(LocalDateTime from, LocalTime kickoff) {
        if (from == null) {
            throw new IllegalArgumentException("An anchor is required to find the next matchday");
        }
        LocalTime start = kickoff == null ? DEFAULT_KICKOFF : kickoff;
        LocalDate day = from.toLocalDate();
        while (!isMatchDay(day) || !day.atTime(start).isAfter(from)) {
            day = day.plusDays(1);
        }
        return day;
    }

    /** The next matchday strictly after {@code matchDay}. */
    public static LocalDate nextMatchDayAfter(LocalDate matchDay) {
        if (matchDay == null) {
            throw new IllegalArgumentException("A matchday is required to find the next one");
        }
        LocalDate day = matchDay.plusDays(1);
        while (!isMatchDay(day)) {
            day = day.plusDays(1);
        }
        return day;
    }

    /**
     * One fantasy round's calendar: the day it is played on, and the three
     * timestamps the {@code fantasyRound} row stores.
     */
    public record Window(LocalDate matchDay,
                         LocalDateTime openDate,
                         LocalDateTime lockDeadline,
                         LocalDateTime endDate) {
    }

    /**
     * {@code count} consecutive matchday windows, the first kicking off
     * strictly after {@code from}.
     *
     * @return the windows in playing order, empty when {@code count} is not
     * positive
     */
    public static List<Window> schedule(LocalDateTime from, int count) {
        if (count <= 0) {
            return List.of();
        }

        List<Window> windows = new ArrayList<>(count);

        LocalDate matchDay = firstMatchDayAfter(from);
        LocalDateTime openDate = from;

        for (int i = 0; i < count; i++) {
            LocalDateTime lockDeadline = matchDay.atTime(DEFAULT_KICKOFF);
            LocalDateTime endDate = matchDay.atTime(DAY_END);

            windows.add(new Window(matchDay, openDate, lockDeadline, endDate));

            // The next window opens the instant this one closes, so the
            // transfer windows tile the calendar without ever overlapping.
            matchDay = nextMatchDayAfter(matchDay);
            openDate = endDate.plusSeconds(1);
        }

        return List.copyOf(windows);
    }

    /**
     * The window a single round occupies when it is played on {@code matchDay}
     * and its transfer window opens at {@code openDate}. Used by the match-day
     * editor and the administrator override, which move one round rather than
     * minting a run of them.
     *
     * <p>{@code openDate} is clamped to the kickoff so a round dragged earlier
     * than its own opening still satisfies {@code chk_fantasyRound_dates}.
     */
    public static Window windowFor(LocalDate matchDay, LocalDateTime openDate) {
        return windowFor(matchDay, openDate, DEFAULT_KICKOFF);
    }

    /**
     * As {@link #windowFor(LocalDate, LocalDateTime)}, for a chosen kickoff.
     * The kickoff becomes the round's {@code lockDeadline}, which is what every
     * fixture in the round reads its date and time from -- so moving the time
     * here moves the whole round, and the two can never drift apart.
     */
    public static Window windowFor(LocalDate matchDay, LocalDateTime openDate, LocalTime kickoff) {
        if (matchDay == null) {
            throw new IllegalArgumentException("A matchday is required to build a window");
        }
        LocalDateTime lockDeadline = matchDay.atTime(kickoff == null ? DEFAULT_KICKOFF : kickoff);
        LocalDateTime endDate = matchDay.atTime(DAY_END);
        LocalDateTime open = (openDate == null || openDate.isAfter(lockDeadline)) ? lockDeadline : openDate;
        return new Window(matchDay, open, lockDeadline, endDate);
    }
}
