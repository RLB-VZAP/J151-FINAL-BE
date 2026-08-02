package com.vzap.trytons.util;

import com.vzap.trytons.enums.LeagueType;

/**
 * The one league-visibility rule, extracted so a new call site can reuse it
 * instead of hand-copying the {@code if} statement a fourth time.
 *
 * <p>Rule: a PUBLIC league is viewable by any authenticated user; a PRIVATE
 * league requires the caller to be an active member or an administrator.
 * Administrators oversee every league without competing in any -- they never
 * count as a "member" on their own, but they may always view.
 *
 * <p>This must stay identical to the inline checks in
 * {@code LeagueServiceImpl.getLeague}, {@code FixtureServiceImpl.assertCanViewLeagueFixtures}
 * and {@code LeagueServiceImpl.listMembers}. Those three have drifted apart twice before
 * (see CLAUDE.md / LESSONS.md) -- once leaving admins unable to open a private league
 * they could already list and start, and once producing a public league page that showed
 * real fixtures and scores beside "0 members". When one of those sites disagrees with this
 * one, fix the disagreement, not the symptom -- do not add a fifth variant.
 */
public final class LeagueVisibility {

    private LeagueVisibility() {
    }

    public static boolean canView(LeagueType leagueType, boolean isAdmin, boolean isActiveMember) {
        if (leagueType == LeagueType.PRIVATE) {
            return isAdmin || isActiveMember;
        }
        return true;
    }

    /**
     * True once a league is no longer running (isActive is FALSE, or -- treated
     * the same, defensively -- unknown/null). Used to keep an archived league's
     * leaderboard read-only: no new current-season row may be created for it,
     * and reads must resolve to the league's own most recent leaderboard season
     * rather than "the" current season.
     */
    public static boolean isArchived(Boolean leagueIsActive) {
        return !Boolean.TRUE.equals(leagueIsActive);
    }
}
