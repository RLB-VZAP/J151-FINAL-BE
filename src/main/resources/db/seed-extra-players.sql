-- ============================================================================
-- Additive seed: extra AVAILABLE players for the tight forward positions.
--
-- The base seed leaves Loose Forward with only 2 available players (one of the
-- three is suspended) while the squad rule requires a minimum of 3, which makes
-- team creation impossible. Hooker (1 available) and Lock (2 available) sit
-- exactly on their minimums with no room to choose. These rows add availability
-- (each isActive with a current ACTIVE availability record) so every position's
-- available pool comfortably meets its minimum. Safe to run more than once only
-- after truncating — it inserts fresh UUIDs each run.
-- ============================================================================

SET @club      = 'efcaec4e-85bf-11f1-9667-b05216330a60';
SET @posLoose  = 'efcd6285-85bf-11f1-9667-b05216330a60';
SET @posHooker = 'efccf162-85bf-11f1-9667-b05216330a60';
SET @posLock   = 'efcd2aed-85bf-11f1-9667-b05216330a60';

SET @lf1 = UUID();
SET @lf2 = UUID();
SET @hk1 = UUID();
SET @lk1 = UUID();

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (@lf1, @club, @posLoose,  'Tank Coetzee', 8.80, 78, 84, 30, 80, 79, 85, 80, 1),
    (@lf2, @club, @posLoose,  'Bandi Nkosi',  9.10, 80, 82, 35, 78, 80, 86, 82, 1),
    (@hk1, @club, @posHooker, 'Wian du Toit', 8.40, 66, 86, 20, 85, 81, 88, 79, 1),
    (@lk1, @club, @posLock,   'Ruben Steyn',  8.70, 58, 90, 15, 87, 82, 90, 78, 1);

INSERT INTO `playerAvailability`
    (availabilityId, playerId, status, effectiveDate, endDate, notes)
VALUES
    (UUID(), @lf1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available loose forward'),
    (UUID(), @lf2, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available loose forward'),
    (UUID(), @hk1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available hooker'),
    (UUID(), @lk1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available lock');
