package com.vzap.trytons.util.fantasyteam;

import com.vzap.trytons.enums.SquadRole;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the bench-assignment bug: FantasyTeamServlet.buildFantasyTeamRequest
 * hardcoded every player to STARTING, so no UI-created team ever got a bench.
 * SquadRoleAssigner is the fix's core: it must always split a legal 20-player
 * squad into exactly 15 STARTING + 5 BENCH, honouring each position's
 * starting quota.
 */
class SquadRoleAssignerTest {

    /** Jarryd XV's real (broken) 20-player composition, from the live DB. */
    private static Map<String, Integer> jarrydXvComposition() {
        Map<String, Integer> composition = new LinkedHashMap<>();
        composition.put("Prop", 3);
        composition.put("Hooker", 2);
        composition.put("Lock", 3);
        composition.put("Loose Forward", 3);
        composition.put("Scrum Half", 2);
        composition.put("Fly Half", 2);
        composition.put("Centre", 2);
        composition.put("Wing", 2);
        composition.put("Fullback", 1);
        return composition;
    }

    /** Builds a squad of UUIDs mapped to position names per the given counts, in a stable order. */
    private static Map<UUID, String> buildSquad(Map<String, Integer> composition) {
        Map<UUID, String> squad = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : composition.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                squad.put(UUID.randomUUID(), entry.getKey());
            }
        }
        return squad;
    }

    @Test
    void jarrydXvsTwentyPlayersSplitIntoExactlyFifteenStartingAndFiveBench() {
        Map<UUID, String> squad = buildSquad(jarrydXvComposition());
        List<UUID> playerIds = List.copyOf(squad.keySet());

        Map<UUID, SquadRole> roles = SquadRoleAssigner.assignRoles(playerIds, squad::get);

        long startingCount = roles.values().stream().filter(r -> r == SquadRole.STARTING).count();
        long benchCount = roles.values().stream().filter(r -> r == SquadRole.BENCH).count();
        assertEquals(SquadRoleAssigner.STARTING_SIZE, startingCount, "expected 15 starters");
        assertEquals(SquadRoleAssigner.BENCH_SIZE, benchCount, "expected 5 bench players");
        assertEquals(SquadRoleAssigner.SQUAD_SIZE, roles.size(), "every player must be assigned a role");
    }

    @Test
    void startingPositionCountsMatchTheSeededQuota() {
        // Verified against the live DB seed (team ec3934f8-...): STARTING is
        // Centre 2, Fly Half 1, Fullback 1, Hooker 1, Lock 2, Loose Forward 3,
        // Prop 2, Scrum Half 1, Wing 2 = 15.
        Map<UUID, String> squad = buildSquad(jarrydXvComposition());
        List<UUID> playerIds = List.copyOf(squad.keySet());

        Map<UUID, SquadRole> roles = SquadRoleAssigner.assignRoles(playerIds, squad::get);

        Map<String, Long> startingByPosition = roles.entrySet().stream()
                .filter(e -> e.getValue() == SquadRole.STARTING)
                .collect(Collectors.groupingBy(e -> squad.get(e.getKey()), Collectors.counting()));

        Map<String, Long> expected = new HashMap<>();
        expected.put("Prop", 2L);
        expected.put("Hooker", 1L);
        expected.put("Lock", 2L);
        expected.put("Loose Forward", 3L);
        expected.put("Scrum Half", 1L);
        expected.put("Fly Half", 1L);
        expected.put("Centre", 2L);
        expected.put("Wing", 2L);
        expected.put("Fullback", 1L);

        assertEquals(expected, startingByPosition);
    }

    @Test
    void benchGetsTheOverflowFromEachOverstockedPosition() {
        // Jarryd's overflow is exactly: 1 Prop, 1 Hooker, 1 Lock, 1 Scrum Half, 1 Fly Half.
        Map<UUID, String> squad = buildSquad(jarrydXvComposition());
        List<UUID> playerIds = List.copyOf(squad.keySet());

        Map<UUID, SquadRole> roles = SquadRoleAssigner.assignRoles(playerIds, squad::get);

        Map<String, Long> benchByPosition = roles.entrySet().stream()
                .filter(e -> e.getValue() == SquadRole.BENCH)
                .collect(Collectors.groupingBy(e -> squad.get(e.getKey()), Collectors.counting()));

        Map<String, Long> expected = new HashMap<>();
        expected.put("Prop", 1L);
        expected.put("Hooker", 1L);
        expected.put("Lock", 1L);
        expected.put("Scrum Half", 1L);
        expected.put("Fly Half", 1L);

        assertEquals(expected, benchByPosition);
    }

    @Test
    void seededFifteenFiveCompositionAlsoAssignsCleanly() {
        // A squad that already sits exactly at quota (no overflow at all)
        // should still produce all-STARTING, zero BENCH.
        Map<String, Integer> exactQuota = new LinkedHashMap<>();
        exactQuota.put("Prop", 2);
        exactQuota.put("Hooker", 1);
        exactQuota.put("Lock", 2);
        exactQuota.put("Loose Forward", 3);
        exactQuota.put("Scrum Half", 1);
        exactQuota.put("Fly Half", 1);
        exactQuota.put("Centre", 2);
        exactQuota.put("Wing", 2);
        exactQuota.put("Fullback", 1);

        Map<UUID, String> squad = buildSquad(exactQuota);
        List<UUID> playerIds = List.copyOf(squad.keySet());

        Map<UUID, SquadRole> roles = SquadRoleAssigner.assignRoles(playerIds, squad::get);

        assertTrue(roles.values().stream().allMatch(r -> r == SquadRole.STARTING));
        assertEquals(15, roles.size());
    }

    @Test
    void unknownPositionNameIsAlwaysBenchedNeverStarting() {
        UUID mystery = UUID.randomUUID();
        Map<UUID, String> squad = Map.of(mystery, "Water Boy");

        Map<UUID, SquadRole> roles = SquadRoleAssigner.assignRoles(List.of(mystery), squad::get);

        assertEquals(SquadRole.BENCH, roles.get(mystery));
    }

    @Test
    void positionAliasesLooseForwardSynonymsShareTheSameQuota() {
        // Mirrors SquadValidationServiceImpl's Flanker/Number Eight synonym handling.
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Map<UUID, String> squad = new LinkedHashMap<>();
        squad.put(ids.get(0), "Loose Forward");
        squad.put(ids.get(1), "Flanker");
        squad.put(ids.get(2), "Number Eight");
        squad.put(ids.get(3), "Flanker");

        Map<UUID, SquadRole> roles = SquadRoleAssigner.assignRoles(ids, squad::get);

        long startingCount = roles.values().stream().filter(r -> r == SquadRole.STARTING).count();
        // Loose-Forward-family quota is 3, so the 4th (whichever alias) is benched.
        assertEquals(3, startingCount);
        assertEquals(SquadRole.BENCH, roles.get(ids.get(3)));
    }
}
