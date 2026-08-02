package com.vzap.trytons.util;

import com.vzap.trytons.enums.LeagueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LeagueVisibility.canView is the one league-visibility rule that
 * LeagueServiceImpl.getLeague, FixtureServiceImpl.assertCanViewLeagueFixtures
 * and LeagueServiceImpl.listMembers must all express identically (see
 * CLAUDE.md). LeaderboardServiceImpl.getLeaderboardForLeague is the newest
 * caller; these cases pin the same truth table those three sites rely on so
 * a future edit here can't silently disagree with them.
 *
 * <p>LeagueVisibility.isArchived backs the "archived leagues are read-only"
 * rule: refreshLeagueLeaderboard must refuse to recompute or create a new
 * leaderboard row for a league with isActive = FALSE.
 */
class LeagueVisibilityTest {

    @Test
    @DisplayName("a public league is viewable by anyone, member or not, admin or not")
    void publicLeagueAlwaysViewable() {
        assertTrue(LeagueVisibility.canView(LeagueType.PUBLIC, false, false));
        assertTrue(LeagueVisibility.canView(LeagueType.PUBLIC, false, true));
        assertTrue(LeagueVisibility.canView(LeagueType.PUBLIC, true, false));
    }

    @Test
    @DisplayName("a private league is viewable by an active member")
    void privateLeagueViewableByMember() {
        assertTrue(LeagueVisibility.canView(LeagueType.PRIVATE, false, true));
    }

    @Test
    @DisplayName("a private league is viewable by an administrator who has not joined")
    void privateLeagueViewableByAdmin() {
        // This is the exact case that was broken: an admin who is not an active
        // member of a private league must still be able to view it (they oversee,
        // they don't compete -- so requiring membership would permanently lock
        // every admin out of every private league's leaderboard).
        assertTrue(LeagueVisibility.canView(LeagueType.PRIVATE, true, false));
    }

    @Test
    @DisplayName("a private league is not viewable by a non-member, non-admin")
    void privateLeagueNotViewableByOutsider() {
        assertFalse(LeagueVisibility.canView(LeagueType.PRIVATE, false, false));
    }

    @Test
    @DisplayName("an active league is not archived")
    void activeLeagueIsNotArchived() {
        assertFalse(LeagueVisibility.isArchived(Boolean.TRUE));
    }

    @Test
    @DisplayName("an explicitly inactive league is archived")
    void inactiveLeagueIsArchived() {
        assertTrue(LeagueVisibility.isArchived(Boolean.FALSE));
    }

    @Test
    @DisplayName("a null isActive is treated as archived, not as active")
    void nullIsActiveIsTreatedAsArchived() {
        // Defensive default: an unknown active-state must not be treated as a
        // green light to create or recompute a leaderboard.
        assertTrue(LeagueVisibility.isArchived(null));
    }
}
