/*
    fix-squad-roles.sql

    Repairs fantasy squads that were saved with a broken STARTING/BENCH
    split: all 20 players marked STARTING, none BENCH.

    How they were created
    --------------------
    `team_player_selection.squadRole` is `ENUM('STARTING','BENCH') NOT NULL
    DEFAULT 'STARTING'`. A valid squad is 20 players = 15 STARTING + 5 BENCH.
    FantasyTeamServlet.buildFantasyTeamRequest (used by both create and
    update) hardcoded every selection to "STARTING" — create-team.jsp has no
    bench UI at all — so no UI-created team ever got a bench.
    SquadValidationServiceImpl.validateSquad only checked squad size (== 20),
    never the STARTING/BENCH split, so these squads passed as "Valid squad".
    FantasyTeamServiceImpl.createTeam/updateTeam and SquadValidationService
    have since been fixed (SquadRoleAssigner normalises the split on every
    save going forward); this script repairs the rows already in the
    database.

    What this script does
    ---------------------
    For every team currently at 20 STARTING / 0 BENCH, applies the same
    starting-XV quota SquadRoleAssigner and SquadValidationServiceImpl use —
    `position.minRequired`, which sums to 15 (Prop 2, Hooker 1, Lock 2,
    Loose Forward 3, Scrum Half 1, Fly Half 1, Centre 2, Wing 2, Fullback 1)
    — and benches the overflow within each over-quota position. Confirmed
    against the live DB: `SELECT positionName, minRequired FROM position`
    matches these figures exactly, and every seeded 15/5 team's STARTING
    rows already sit at exactly this per-position count.

    Within an over-quota position the players with the latest selectedDate
    are the ones benched (ties broken by selectionId), so the choice is
    deterministic rather than relying on undefined row order.

    Safety
    ------
    * Idempotent — only touches teams currently at 20 STARTING / 0 BENCH, so
      re-running after a clean fix updates 0 rows.
    * Touches only `team_player_selection.squadRole`. No other column, and no
      other table.
    * `SHOW TRIGGERS` on `team_player_selection` returns zero rows — this
      UPDATE is not constrained by any trigger.
    * Does not reference `league` or `leagueMembership` at all, so it cannot
      touch the two protected leagues (ec424e68-..., ec424f44-...) even
      indirectly.
    * NOT executed by this script's author — review the BEFORE/AFTER reports
      and run it yourself:
      mysql -u <user> -p tryton_fantasy_rugby < fix-squad-roles.sql
*/

USE `tryton_fantasy_rugby`;

/* ------------------------------------------------------------------ *
 * 1. BEFORE — which teams are broken, and what their squad looks like.
 * ------------------------------------------------------------------ */
SELECT 'BEFORE: teams at 20 STARTING / 0 BENCH (to be repaired)' AS report;

SELECT ft.`teamId`,
       ft.`teamName`,
       SUM(tps.`squadRole` = 'STARTING') AS startingCount,
       SUM(tps.`squadRole` = 'BENCH')    AS benchCount
FROM `team_player_selection` tps
JOIN `fantasyTeam` ft ON ft.`teamId` = tps.`teamId`
GROUP BY ft.`teamId`, ft.`teamName`
HAVING SUM(tps.`squadRole` = 'STARTING') = 20
   AND SUM(tps.`squadRole` = 'BENCH') = 0
ORDER BY ft.`teamName`;

SELECT 'BEFORE: per-position breakdown for those teams' AS report;

SELECT tps.`teamId`, pos.`positionName`, COUNT(*) AS playerCount, pos.`minRequired` AS startingQuota
FROM `team_player_selection` tps
JOIN `player` p ON p.`playerId` = tps.`playerId`
JOIN `position` pos ON pos.`positionId` = p.`positionId`
WHERE tps.`teamId` IN (
    SELECT teamId
    FROM `team_player_selection`
    GROUP BY teamId
    HAVING SUM(squadRole = 'STARTING') = 20 AND SUM(squadRole = 'BENCH') = 0
)
GROUP BY tps.`teamId`, pos.`positionName`, pos.`minRequired`
ORDER BY tps.`teamId`, pos.`positionName`;

/* ------------------------------------------------------------------ *
 * 2. FIX — bench the overflow within each over-quota position.
 *
 * Ranks each broken team's selections within their position, most recently
 * selected first; anything ranked past that position's minRequired (its
 * starting-XV quota) is overflow and gets benched.
 * ------------------------------------------------------------------ */
START TRANSACTION;

UPDATE `team_player_selection` tps
JOIN (
    SELECT
        tps2.`selectionId` AS selectionId,
        ROW_NUMBER() OVER (
            PARTITION BY tps2.`teamId`, pos.`positionId`
            ORDER BY tps2.`selectedDate` DESC, tps2.`selectionId` DESC
        ) AS positionRank,
        pos.`minRequired` AS startingQuota
    FROM `team_player_selection` tps2
    JOIN `player` p ON p.`playerId` = tps2.`playerId`
    JOIN `position` pos ON pos.`positionId` = p.`positionId`
    WHERE tps2.`teamId` IN (
        SELECT teamId
        FROM `team_player_selection`
        GROUP BY teamId
        HAVING SUM(squadRole = 'STARTING') = 20 AND SUM(squadRole = 'BENCH') = 0
    )
) AS ranked ON ranked.selectionId = tps.`selectionId`
SET tps.`squadRole` = 'BENCH'
WHERE ranked.positionRank > ranked.startingQuota;

COMMIT;

/* ------------------------------------------------------------------ *
 * 3. AFTER — verification.
 * ------------------------------------------------------------------ */
SELECT 'AFTER: teams still at 20 STARTING / 0 BENCH (expect 0 rows)' AS report;

SELECT ft.`teamId`, ft.`teamName`,
       SUM(tps.`squadRole` = 'STARTING') AS startingCount,
       SUM(tps.`squadRole` = 'BENCH')    AS benchCount
FROM `team_player_selection` tps
JOIN `fantasyTeam` ft ON ft.`teamId` = tps.`teamId`
GROUP BY ft.`teamId`, ft.`teamName`
HAVING SUM(tps.`squadRole` = 'STARTING') = 20
   AND SUM(tps.`squadRole` = 'BENCH') = 0;

SELECT 'AFTER: every team must now be exactly 15 STARTING / 5 BENCH (expect 0 rows failing)' AS report;

SELECT ft.`teamId`, ft.`teamName`,
       SUM(tps.`squadRole` = 'STARTING') AS startingCount,
       SUM(tps.`squadRole` = 'BENCH')    AS benchCount
FROM `team_player_selection` tps
JOIN `fantasyTeam` ft ON ft.`teamId` = tps.`teamId`
GROUP BY ft.`teamId`, ft.`teamName`
HAVING NOT (SUM(tps.`squadRole` = 'STARTING') = 15 AND SUM(tps.`squadRole` = 'BENCH') = 5);
