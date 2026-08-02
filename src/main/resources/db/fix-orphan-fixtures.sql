/*
    fix-orphan-fixtures.sql

    Repairs "hollow" fixtures: rows marked COMPLETED or PROCESSED that have no
    current `matchResult` behind them, and therefore no `playerStatistics`, no
    `fantasyPoints` and no `match_team_score` either.

    How they were created
    --------------------
    `PUT /fixtures/{id}/status` (FixtureServiceImpl.updateFixtureStatus) used to
    accept SIMULATING -> COMPLETED and COMPLETED -> PROCESSED as plain
    administrative moves. It wrote nothing but `fixture.status`, and stamped
    `simulationDate = NOW()` on the way to COMPLETED. That stamp is what made the
    row survive: `chk_fixture_simulation_date` demands a non-NULL simulationDate
    for COMPLETED/PROCESSED, and forging it satisfied the CHECK. No trigger could
    object either — `trg_match_team_score_insert` and `trg_matchResult_score_update`
    fire on INSERT/UPDATE of those tables, and writing zero rows trips zero
    triggers. Both transitions have since been removed from the service.

    What this script does
    ---------------------
    Resets each hollow fixture to UPCOMING with `simulationDate = NULL`, so it can
    be locked and simulated legitimately later via
    POST /simulations/fixtures/{id} then POST /match-processing/fixtures/{id}.

    It does NOT invent a matchResult. `trg_matchResult_score_update` and
    `trg_match_team_score_insert` only accept scores that ScoringCalculator
    produced; a hand-written result would either be rejected or, worse, poison the
    leaderboard. Simulation is the only legitimate source.

    Safety
    ------
    * Idempotent — re-running it after a clean run updates 0 rows.
    * Touches only `fixture.status` and `fixture.simulationDate`.
    * Never touches a fixture that has any matchResult history at all (see the
      stricter EXISTS below), nor the two protected leagues.
    * `trg_fixture_integrity_update` allows this update: it only guards changes to
      leagueId / roundId / team ids, league membership, and one-fixture-per-round.

    Run:  mysql -u <user> -p tryton_fantasy_rugby < fix-orphan-fixtures.sql
*/

USE `tryton_fantasy_rugby`;

/* ------------------------------------------------------------------ *
 * 1. BEFORE — what is about to be reset.
 * ------------------------------------------------------------------ */
SELECT 'BEFORE: hollow fixtures to be reset' AS report;

SELECT f.`fixtureId`,
       f.`leagueId`,
       f.`status`,
       f.`fixtureDate`,
       f.`simulationDate`,
       (SELECT COUNT(*) FROM `matchResult` mr WHERE mr.`fixtureId` = f.`fixtureId`) AS matchResultRows
FROM `fixture` f
WHERE f.`status` IN ('COMPLETED', 'PROCESSED')
  AND NOT EXISTS (SELECT 1
                  FROM `matchResult` mr
                  WHERE mr.`fixtureId` = f.`fixtureId`
                    AND mr.`isCurrent` = TRUE)
  AND NOT EXISTS (SELECT 1
                  FROM `matchResult` mr
                  WHERE mr.`fixtureId` = f.`fixtureId`)
  AND f.`leagueId` NOT IN ('ec424e68-8cfb-11f1-b5b1-9ef187dae662',
                           'ec424f44-8cfb-11f1-b5b1-9ef187dae662')
ORDER BY f.`fixtureDate`;

/* ------------------------------------------------------------------ *
 * 2. REVIEW — the in-between case, reported but NOT touched.
 *
 * A fixture with matchResult rows of which none is current is not hollow: it
 * has real simulation history (e.g. a controlled re-simulation that left no row
 * flagged current). Resetting it would discard that history, so it is listed
 * for a human to decide. Expect zero rows.
 * ------------------------------------------------------------------ */
SELECT 'REVIEW: has matchResult history but none current — NOT reset' AS report;

SELECT f.`fixtureId`,
       f.`leagueId`,
       f.`status`,
       f.`simulationDate`,
       (SELECT COUNT(*) FROM `matchResult` mr WHERE mr.`fixtureId` = f.`fixtureId`) AS matchResultRows
FROM `fixture` f
WHERE f.`status` IN ('COMPLETED', 'PROCESSED')
  AND NOT EXISTS (SELECT 1
                  FROM `matchResult` mr
                  WHERE mr.`fixtureId` = f.`fixtureId`
                    AND mr.`isCurrent` = TRUE)
  AND EXISTS (SELECT 1
              FROM `matchResult` mr
              WHERE mr.`fixtureId` = f.`fixtureId`)
ORDER BY f.`fixtureDate`;

/* ------------------------------------------------------------------ *
 * 3. FIX — reset the hollow fixtures.
 * ------------------------------------------------------------------ */
START TRANSACTION;

UPDATE `fixture` f
SET f.`status`         = 'UPCOMING',
    f.`simulationDate` = NULL
WHERE f.`status` IN ('COMPLETED', 'PROCESSED')
  AND NOT EXISTS (SELECT 1
                  FROM `matchResult` mr
                  WHERE mr.`fixtureId` = f.`fixtureId`)
  AND f.`leagueId` NOT IN ('ec424e68-8cfb-11f1-b5b1-9ef187dae662',
                           'ec424f44-8cfb-11f1-b5b1-9ef187dae662');

COMMIT;

/* ------------------------------------------------------------------ *
 * 4. AFTER — verification. Both queries must return zero rows.
 * ------------------------------------------------------------------ */
SELECT 'AFTER: remaining hollow fixtures (expect 0 rows)' AS report;

SELECT f.`fixtureId`, f.`leagueId`, f.`status`, f.`simulationDate`
FROM `fixture` f
WHERE f.`status` IN ('COMPLETED', 'PROCESSED')
  AND NOT EXISTS (SELECT 1
                  FROM `matchResult` mr
                  WHERE mr.`fixtureId` = f.`fixtureId`)
  AND f.`leagueId` NOT IN ('ec424e68-8cfb-11f1-b5b1-9ef187dae662',
                           'ec424f44-8cfb-11f1-b5b1-9ef187dae662');

SELECT 'AFTER: PROCESSED fixtures without exactly 2 match_team_score rows (expect 0 rows)' AS report;

SELECT f.`fixtureId`,
       f.`leagueId`,
       COUNT(mts.`scoreId`) AS scoreRows
FROM `fixture` f
         JOIN `matchResult` mr
              ON mr.`fixtureId` = f.`fixtureId` AND mr.`isCurrent` = TRUE
         LEFT JOIN `match_team_score` mts
                   ON mts.`resultId` = mr.`resultId`
WHERE f.`status` = 'PROCESSED'
GROUP BY f.`fixtureId`, f.`leagueId`
HAVING COUNT(mts.`scoreId`) <> 2;
