package com.vzap.trytons.util.fantasyteam;

import com.vzap.trytons.enums.SquadRole;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Splits a 20-player fantasy squad into a 15-man starting XV and a 5-man
 * bench, by position, mirroring the {@code position.minRequired} column and
 * the {@code MIN_*} constants in {@code SquadValidationServiceImpl}
 * (verified against the seeded database on 2026-08-02):
 *
 * <pre>
 * Prop 2, Hooker 1, Lock 2, Loose Forward 3, Scrum Half 1,
 * Fly Half 1, Centre 2, Wing 2, Fullback 1  =&gt; 15
 * </pre>
 *
 * <p>Every seeded squad already satisfies {@code minRequired &lt;= actual
 * &lt;= maxAllowed} per position (enforced by
 * {@code SquadValidationServiceImpl.validatePositionRules}), so once that
 * check passes, filling each position's quota up to {@code minRequired} and
 * benching the rest always yields exactly 15 STARTING + 5 BENCH — the sum of
 * the quotas below is 15 by construction. This assigner does not itself
 * re-check squad size or position min/max; callers should validate the squad
 * first (as {@code FantasyTeamServiceImpl} already does) and only need this
 * to decide *which* players start.
 *
 * <p>Pure and side-effect free, in the same spirit as {@code PoolAllocator}
 * and {@code MatchdayCalendar}, so it is unit-testable without a database.
 */
public final class SquadRoleAssigner {

    public static final int SQUAD_SIZE = 20;
    public static final int STARTING_SIZE = 15;
    public static final int BENCH_SIZE = 5;

    /**
     * Starting-XV quota per canonical position name. Mirrors
     * {@code position.minRequired} in schema.sql / seed data.
     */
    private static final Map<String, Integer> STARTING_QUOTA = Map.of(
            "Prop", 2,
            "Hooker", 1,
            "Lock", 2,
            "Loose Forward", 3,
            "Scrum Half", 1,
            "Fly Half", 1,
            "Centre", 2,
            "Wing", 2,
            "Fullback", 1
    );

    /**
     * Alternate names for "Loose Forward" some callers may use, matching the
     * synonym handling already present in
     * {@code SquadValidationServiceImpl.validatePositionRules}.
     */
    private static final Map<String, String> POSITION_ALIASES = Map.of(
            "Flanker", "Loose Forward",
            "Number Eight", "Loose Forward"
    );

    private SquadRoleAssigner() {
    }

    /**
     * Assigns a {@link SquadRole} to every player, filling each position's
     * starting quota in the order the players are given and benching any
     * overflow (including players in a position not recognised above, which
     * have no starting quota and are always benched).
     *
     * @param players        the squad's players, in the order roles should be
     *                       assigned when a position has more players than
     *                       its quota
     * @param positionNameOf resolves a player to its position name
     * @return a map from player to assigned role, iteration-ordered the same
     * as {@code players}
     */
    public static <T> Map<T, SquadRole> assignRoles(List<T> players, Function<T, String> positionNameOf) {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(positionNameOf, "positionNameOf");

        Map<String, Integer> remainingQuota = new LinkedHashMap<>(STARTING_QUOTA);
        Map<T, SquadRole> roles = new LinkedHashMap<>();

        for (T player : players) {
            String positionName = positionNameOf.apply(player);
            String canonicalName = POSITION_ALIASES.getOrDefault(positionName, positionName);
            int quota = remainingQuota.getOrDefault(canonicalName, 0);

            if (quota > 0) {
                roles.put(player, SquadRole.STARTING);
                remainingQuota.put(canonicalName, quota - 1);
            } else {
                roles.put(player, SquadRole.BENCH);
            }
        }

        return roles;
    }
}
