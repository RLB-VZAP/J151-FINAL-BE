/* =====================================================================
   Reset the competition layer so every league can be replayed from
   scratch under the current scheduling and scoring rules.

   KEEPS: users, administrators, fantasy teams and their squads
   (team_player_selection), leagues, league memberships, players, clubs,
   positions, scoring rules, pricing and tournament settings.

   REMOVES: every fixture and everything derived from one -- match
   results, per-player statistics, fantasy points, team scores,
   leaderboards, rankings, tournaments and their pools/standings -- plus
   stale notifications. Leagues are returned to FORMING so they can be
   started again.

   ALSO KEPT, on purpose: the fantasyRound calendar, transfers, round
   locks and locked round selections. See the note at step 4 -- they are
   inert now that rounds are minted per league, and clearing them would
   mean either dropping a safety trigger or desyncing team budgets.

   Delete order is dictated by the foreign keys, several of which are
   RESTRICT rather than CASCADE:
     fantasyPoints -> playerStatistics   RESTRICT  (points before stats)
     matchResult   -> fixture            RESTRICT  (results before fixtures)

   Run with:
     podman exec -i trytons-mysql mysql -uroot -proot tryton_fantasy_rugby \
       < reset-competition.sql
   ===================================================================== */

SELECT 'BEFORE' AS report;
SELECT 'fixtures' AS what, COUNT(*) AS n FROM `fixture`
UNION ALL SELECT 'matchResult', COUNT(*) FROM `matchResult`
UNION ALL SELECT 'match_team_score', COUNT(*) FROM `match_team_score`
UNION ALL SELECT 'playerStatistics', COUNT(*) FROM `playerStatistics`
UNION ALL SELECT 'fantasyPoints', COUNT(*) FROM `fantasyPoints`
UNION ALL SELECT 'leaderboard', COUNT(*) FROM `leaderboard`
UNION ALL SELECT 'ranking', COUNT(*) FROM `ranking`
UNION ALL SELECT 'tournament', COUNT(*) FROM `tournament`
UNION ALL SELECT 'fantasyRound (kept)', COUNT(*) FROM `fantasyRound`
UNION ALL SELECT 'leagues not FORMING', COUNT(*) FROM `league` WHERE status <> 'FORMING';

/* Preserved, and re-asserted at the end so a mistake here is loud. */
SELECT 'PRESERVED (must be unchanged after)' AS report;
SELECT 'user' AS what, COUNT(*) AS n FROM `user`
UNION ALL SELECT 'fantasyTeam', COUNT(*) FROM `fantasyTeam`
UNION ALL SELECT 'team_player_selection', COUNT(*) FROM `team_player_selection`
UNION ALL SELECT 'league', COUNT(*) FROM `league`
UNION ALL SELECT 'leagueMembership', COUNT(*) FROM `leagueMembership`;

START TRANSACTION;

/* 1. Scoring output, innermost first. */
DELETE FROM `fantasyPoints`;
DELETE FROM `match_team_score`;
DELETE FROM `playerStatistics`;
DELETE FROM `matchResult`;

/* 2. Fixtures and the tournament structures that own them. */
DELETE FROM `fixture`;
DELETE FROM `tournament_standing`;
DELETE FROM `tournament_pool_member`;
DELETE FROM `tournament_pool`;
DELETE FROM `tournament`;

/* 3. Leaderboards. ranking cascades from leaderboard, deleted explicitly
      so the row count in the AFTER report is meaningful. */
DELETE FROM `ranking`;
DELETE FROM `leaderboard`;

/* 4. The round calendar is deliberately LEFT ALONE.

      trg_round_selection_immutable_delete blocks every delete from
      fantasy_team_round_selection unconditionally -- those rows are the
      audit record of which squad was locked in for a round, and dropping
      a safety trigger to clear them would be the wrong trade. Rounds are
      minted per league on demand now (allocateRounds no longer claims
      pre-existing ones), so leftover rounds are inert: a restarted league
      mints its own future Wed/Sat/Sun rounds and never touches these.

      transfer rows are kept for the same reason plus a harder one --
      fantasyTeam.remainingBudget is stored, not derived, so deleting a
      transfer without reversing its cost would desync every affected
      team's budget.

   5. Stale notifications, which point at fixtures that no longer exist. */
DELETE FROM `notification`;

/* 6. Every league becomes startable again. */
UPDATE `league` SET status = 'FORMING', startedAt = NULL;

COMMIT;

SELECT 'AFTER (all zero)' AS report;
SELECT 'fixtures' AS what, COUNT(*) AS n FROM `fixture`
UNION ALL SELECT 'matchResult', COUNT(*) FROM `matchResult`
UNION ALL SELECT 'match_team_score', COUNT(*) FROM `match_team_score`
UNION ALL SELECT 'playerStatistics', COUNT(*) FROM `playerStatistics`
UNION ALL SELECT 'fantasyPoints', COUNT(*) FROM `fantasyPoints`
UNION ALL SELECT 'leaderboard', COUNT(*) FROM `leaderboard`
UNION ALL SELECT 'ranking', COUNT(*) FROM `ranking`
UNION ALL SELECT 'tournament', COUNT(*) FROM `tournament`
UNION ALL SELECT 'fantasyRound (kept)', COUNT(*) FROM `fantasyRound`
UNION ALL SELECT 'leagues not FORMING', COUNT(*) FROM `league` WHERE status <> 'FORMING';

SELECT 'AFTER: preserved counts (must match BEFORE)' AS report;
SELECT 'user' AS what, COUNT(*) AS n FROM `user`
UNION ALL SELECT 'fantasyTeam', COUNT(*) FROM `fantasyTeam`
UNION ALL SELECT 'team_player_selection', COUNT(*) FROM `team_player_selection`
UNION ALL SELECT 'league', COUNT(*) FROM `league`
UNION ALL SELECT 'leagueMembership', COUNT(*) FROM `leagueMembership`;

SELECT 'AFTER: every squad must still be 15 STARTING / 5 BENCH (expect 0 rows)' AS report;
SELECT t.teamName, SUM(s.squadRole = 'STARTING') AS xv, SUM(s.squadRole = 'BENCH') AS bench
FROM `team_player_selection` s
JOIN `fantasyTeam` t ON t.teamId = s.teamId
GROUP BY t.teamName
HAVING xv <> 15 OR bench <> 5;
