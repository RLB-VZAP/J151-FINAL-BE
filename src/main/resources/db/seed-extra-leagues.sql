-- ============================================================================
--  Additive league seed — extra public/private leagues, with one chosen user
--  joined to three of them.
--
--  Safe to run against an already-seeded database: it only INSERTs, never
--  drops or truncates, and every insert is guarded so a re-run cannot violate
--  the unique constraints (uk_leagueMembership_user, uk_ranking_position,
--  uk_leaderboard_scope_season, uk_league_code).
--
--  HOW TO RUN
--    mysql -u <user> -p tryton_fantasy_rugby < seed-extra-leagues.sql
--
--  SET YOUR USERNAME on the @meUsername line below. It must match
--  user.username of the account you log into the app with.
-- ============================================================================

USE `tryton_fantasy_rugby`;

-- ---------------------------------------------------------------------------
-- 0. Before/after picture, so it is obvious whether the rows were already there
-- ---------------------------------------------------------------------------
SELECT 'BEFORE' AS stage,
       COUNT(*)                                    AS leagues,
       SUM(leagueType = 'PUBLIC')                  AS public_leagues,
       SUM(leagueType = 'PRIVATE')                 AS private_leagues
FROM `league`;

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- 1. Who am I?  <<< CHANGE THIS to your login username
-- ---------------------------------------------------------------------------
SET @meUsername = 'ChrissyB';

SET @meId = (SELECT userId FROM `user` WHERE username = @meUsername);

-- A membership needs a team (leagueMembership.teamId is NOT NULL), and
-- uk_fantasyTeam_owner allows exactly one team per user. Create one only if
-- this account does not already have it. Budget matches
-- FantasyTeamServiceImpl.INITIAL_BUDGET, which is in millions.
INSERT INTO `fantasyTeam` (teamId, owner_user_id, teamName, remainingBudget, isValid)
SELECT UUID(), @meId, CONCAT(@meUsername, '''s XV'), 196.00, TRUE
FROM DUAL
WHERE @meId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `fantasyTeam` WHERE owner_user_id = @meId);

SET @myTeamId = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @meId);

-- ---------------------------------------------------------------------------
-- 2. New leagues — 3 public, 2 private
-- ---------------------------------------------------------------------------
SET @lgHighveld = UUID();
SET @lgCoastal  = UUID();
SET @lgVarsity  = UUID();
SET @lgOffice   = UUID();
SET @lgOldBoys  = UUID();

-- PUBLIC leagues must have a NULL leagueCode (chk_league_code_type), and
-- trg_league_manager_insert forbids setting manager_user_id on INSERT: the
-- manager has to be an active member first. Managers are assigned in step 3c.
INSERT INTO `league` (leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, maxMembers)
SELECT * FROM (
    SELECT @lgHighveld AS leagueId, NULL AS manager_user_id, 'Highveld Heroes' AS leagueName,
           'Open league for Bulls, Lions and Cheetahs supporters. Weekly bragging rights on the line.' AS description,
           'PUBLIC' AS leagueType, NULL AS leagueCode, 24 AS maxMembers
    UNION ALL
    SELECT @lgCoastal, NULL, 'Coastal Cup',
           'Sharks and Stormers country. A relaxed public league for the coastal sides.',
           'PUBLIC', NULL, 32
    UNION ALL
    SELECT @lgVarsity, NULL, 'Varsity Challenge',
           'Student league open to everyone. Fast turnover and plenty of transfers.',
           'PUBLIC', NULL, 16
    UNION ALL
    SELECT @lgOffice, NULL, 'Office Rugby Pool',
           'Private league for the team at work. Ask the manager for the code.',
           'PRIVATE', 'OFF123', 12
    UNION ALL
    SELECT @lgOldBoys, NULL, 'Old Boys XV',
           'Invite-only league for the alumni side. Long memories, longer arguments.',
           'PRIVATE', 'OLD456', 10
) AS newLeagues
-- Skip any league whose name is already present, so a re-run is a no-op.
WHERE NOT EXISTS (SELECT 1 FROM `league` l WHERE l.leagueName = newLeagues.leagueName);

-- Re-resolve the ids: on a re-run the UUID()s above were not inserted, so read
-- back whatever is actually in the table.
SET @lgHighveld = (SELECT leagueId FROM `league` WHERE leagueName = 'Highveld Heroes');
SET @lgCoastal  = (SELECT leagueId FROM `league` WHERE leagueName = 'Coastal Cup');
SET @lgVarsity  = (SELECT leagueId FROM `league` WHERE leagueName = 'Varsity Challenge');
SET @lgOffice   = (SELECT leagueId FROM `league` WHERE leagueName = 'Office Rugby Pool');
SET @lgOldBoys  = (SELECT leagueId FROM `league` WHERE leagueName = 'Old Boys XV');

-- ---------------------------------------------------------------------------
-- 3. Memberships
--    You join three: Highveld Heroes and Coastal Cup (public) and Office Rugby
--    Pool (private). Varsity Challenge and Old Boys XV are deliberately left
--    without you, so the Discover tab and the join-by-code path both have
--    something to show.
-- ---------------------------------------------------------------------------

-- 3a. You
INSERT INTO `leagueMembership` (membershipId, leagueId, registered_user_id, teamId)
SELECT UUID(), lg.leagueId, @meId, @myTeamId
FROM (SELECT @lgHighveld AS leagueId
      UNION ALL SELECT @lgCoastal
      UNION ALL SELECT @lgOffice) AS lg
WHERE @meId IS NOT NULL
  AND @myTeamId IS NOT NULL
  AND lg.leagueId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `leagueMembership` m
                  WHERE m.leagueId = lg.leagueId AND m.registered_user_id = @meId);

-- 3b. Other seeded managers, so the member counts are not all 1.
--     Only users who already have a team can join, and never you twice.
INSERT INTO `leagueMembership` (membershipId, leagueId, registered_user_id, teamId)
SELECT UUID(), lg.leagueId, u.userId, ft.teamId
FROM `user` u
JOIN `fantasyTeam` ft ON ft.owner_user_id = u.userId
JOIN (SELECT @lgHighveld AS leagueId, 'sarah' AS username
      UNION ALL SELECT @lgHighveld, 'mike'
      UNION ALL SELECT @lgHighveld, 'emma'
      UNION ALL SELECT @lgCoastal,  'david'
      UNION ALL SELECT @lgCoastal,  'sarah'
      UNION ALL SELECT @lgOffice,   'mike'
      UNION ALL SELECT @lgVarsity,  'emma'
      UNION ALL SELECT @lgVarsity,  'david'
      UNION ALL SELECT @lgOldBoys,  'sarah') AS lg ON lg.username = u.username
WHERE lg.leagueId IS NOT NULL
  AND (@meId IS NULL OR u.userId <> @meId)
  AND NOT EXISTS (SELECT 1 FROM `leagueMembership` m
                  WHERE m.leagueId = lg.leagueId AND m.registered_user_id = u.userId)
  AND NOT EXISTS (SELECT 1 FROM `leagueMembership` m
                  WHERE m.leagueId = lg.leagueId AND m.teamId = ft.teamId);

-- 3c. Now that the memberships exist, the manager can be assigned.
--     trg_league_manager_update still checks the membership, so this only
--     succeeds for leagues you actually joined above.
UPDATE `league` l
SET l.manager_user_id = @meId
WHERE l.leagueName IN ('Highveld Heroes', 'Office Rugby Pool')
  AND l.manager_user_id IS NULL
  AND @meId IS NOT NULL
  AND EXISTS (SELECT 1 FROM `leagueMembership` m
              WHERE m.leagueId = l.leagueId AND m.registered_user_id = @meId AND m.isActive = TRUE);

-- ---------------------------------------------------------------------------
-- 4. LEAGUE-scope leaderboards for the new leagues, so the cards on the
--    leagues page have mini standings to show.
--    Season matches the existing seed ('2026'); uk_leaderboard_scope_season
--    allows one per league per season.
-- ---------------------------------------------------------------------------
INSERT INTO `leaderboard` (leaderboardId, leagueId, season, scope)
SELECT UUID(), lg.leagueId, '2026', 'LEAGUE'
FROM (SELECT @lgHighveld AS leagueId
      UNION ALL SELECT @lgCoastal
      UNION ALL SELECT @lgVarsity
      UNION ALL SELECT @lgOffice
      UNION ALL SELECT @lgOldBoys) AS lg
WHERE lg.leagueId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `leaderboard` lb
                  WHERE lb.leagueId = lg.leagueId AND lb.season = '2026');

-- ---------------------------------------------------------------------------
-- 5. Rankings for every member of the new leagues.
--    currentRanking must be unique per leaderboard (uk_ranking_position), so
--    positions come from ROW_NUMBER(). Points descend with rank so the table
--    reads consistently, and previousRanking is one lower to give the rank
--    movement chips something to show.
-- ---------------------------------------------------------------------------
INSERT INTO `ranking` (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
                       matchesPlayed, matchesWon, matchesDrawn, matchesLost,
                       pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
SELECT UUID(), r.leaderboardId, r.teamId, r.rn, r.rn + 1,
       3, 3 - r.rn % 3, 0, r.rn % 3,
       60 - (r.rn * 4), 40 + (r.rn * 3), 12 - (r.rn * 2), 48 - (r.rn * 6)
FROM (
    SELECT lb.leaderboardId,
           m.teamId,
           ROW_NUMBER() OVER (PARTITION BY lb.leaderboardId ORDER BY m.joinDate, m.teamId) AS rn
    FROM `leaderboard` lb
    JOIN `leagueMembership` m ON m.leagueId = lb.leagueId AND m.isActive = TRUE
    WHERE lb.season = '2026'
      AND lb.scope = 'LEAGUE'
      AND lb.leagueId IN (@lgHighveld, @lgCoastal, @lgVarsity, @lgOffice, @lgOldBoys)
) AS r
WHERE NOT EXISTS (SELECT 1 FROM `ranking` rk
                  WHERE rk.leaderboardId = r.leaderboardId AND rk.teamId = r.teamId);

COMMIT;

-- ---------------------------------------------------------------------------
-- 6. Verify
-- ---------------------------------------------------------------------------
SELECT 'AFTER' AS stage,
       COUNT(*)                    AS leagues,
       SUM(leagueType = 'PUBLIC')  AS public_leagues,
       SUM(leagueType = 'PRIVATE') AS private_leagues
FROM `league`;

SELECT l.leagueName,
       l.leagueType,
       l.maxMembers,
       COUNT(m.membershipId)                                              AS members,
       MAX(m.registered_user_id = @meId)                                  AS you_are_a_member
FROM `league` l
LEFT JOIN `leagueMembership` m ON m.leagueId = l.leagueId AND m.isActive = TRUE
GROUP BY l.leagueId, l.leagueName, l.leagueType, l.maxMembers
ORDER BY l.leagueType, l.leagueName;
