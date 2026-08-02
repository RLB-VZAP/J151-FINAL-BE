/* ============================================================================
   Fantasy TryTons League  --  MERGED PRESENTATION SEED
   ----------------------------------------------------------------------------
   Single self-contained script. Run it against a freshly-created schema:

       mysql -u <user> -p tryton_fantasy_rugby < schema.sql
       mysql -u <user> -p tryton_fantasy_rugby < seed-presentation.sql

   It merges, in one session:
     Section 1  base seed.sql            (Cheetahs/Pumas club bug fixed)
     Section 2  seed-extra-players.sql   (rewired to this seed's real IDs)
     Section 3  presentation accounts    (3 admins, 3 users, 8 named people)
     Section 4  presentation leagues     (showcase + sunrise + midnight test)
     Section 5  seed-extra-leagues.sql   (pointed at user 'christan')

   -------------------------------  LOGIN CREDENTIALS  -------------------------
   ADMINISTRATORS  (password: Admin@123)
       admin1 / admin1@trytons.com
       admin2 / admin2@trytons.com
       admin3 / admin3@trytons.com
   DEMO USERS      (password: User@123)
       user1  / user1@trytons.com
       user2  / user2@trytons.com
       user3  / user3@trytons.com
   NAMED PARTICIPANTS (password: Trytons@123)
       christan  / christan@trytons.com     magdeli   / magdeli@trytons.com
       lindsay   / lindsay@trytons.com      sameer    / sameer@trytons.com
       jaunte    / jaunte@trytons.com       timothy   / timothy@trytons.com
       jarryd    / jarryd@trytons.com       sulaimaan / sulaimaan@trytons.com

   (Base seed accounts also remain: admin/Admin@12345, john/John@12345,
    sarah/Sarah@12345, mike/Mike@12345, emma/Emma@12345, david/David@12345,
    lisa/Lisa@12345, tom/Tom@12345.)
   ============================================================================ */

USE `tryton_fantasy_rugby`;

START TRANSACTION;

SET
@adminId = UUID();
    SET
@johnId = UUID();
    SET
@sarahId = UUID();
    SET
@mikeId = UUID();
    SET
@emmaId = UUID();
    SET
@davidId = UUID();
    SET
@lisaId = UUID();
    SET
@tomId = UUID();
/*
admin	admin@tritan.com	Admin@12345
john	john@test.com	John@12345
sarah	sarah@test.com	Sarah@12345
mike	mike@test.com	Mike@12345
emma	emma@test.com	Emma@12345
david	david@test.com	David@12345
lisa	lisa@test.com	Lisa@12345
tom	tom@test.com	Tom@12345
 */
INSERT INTO `user`
    (userId, email, passwordHash, username, role)
VALUES (@adminId, 'admin@tritan.com', '$2a$12$ToCAGmBUmoJd5p1LWWewHeCDD8sy/lQmYzUUCv9Wf701EtxoqIpIC', 'admin',
        'ADMINISTRATOR'),
       (@johnId, 'john@test.com', '$2a$12$BwiRPiDDJb81VBpIJ.5D4u/xpaAY9fqM/PJzqsDAz703vHwPEXt4W', 'john',
        'REGISTERED_USER'),
       (@sarahId, 'sarah@test.com', '$2a$12$KxC6y3TRYzeHaVqEXoyoce5HK4Z4aefBlfMJtvoPouCsUuM92x7XO', 'sarah',
        'REGISTERED_USER'),
       (@mikeId, 'mike@test.com', '$2a$12$nc7G2MHlgbX7Cc5NlmiHKOhq1ZgKUBsGJ7k/l2.6e0YaWG4koPNie', 'mike',
        'REGISTERED_USER'),
       (@emmaId, 'emma@test.com', '$2a$12$ETILuthQlI3u/Yj1KZjZ9evmcyfyfEawvD0Hqe/KmLcgarWPPHHHy', 'emma',
        'REGISTERED_USER'),
       (@davidId, 'david@test.com', '$2a$12$.NzFJ.9//Ywv0ksWWno5V.8jrtWacV./tR2ZC31ySrUp36CxepChi', 'david',
        'REGISTERED_USER'),
       (@lisaId, 'lisa@test.com', '$2a$12$uWpaMQBKDHVQYBRvGSnuoubl8.CcC9IkyVKFSfVQIjP9ceJrSx.BG', 'lisa',
        'REGISTERED_USER'),
       (@tomId, 'tom@test.com', '$2a$12$F30/FCjnU6wbrvgMoaL2j.RaU8YQdqhz51NgaGBAoLNFCg.OIzmY6', 'tom',
        'REGISTERED_USER');

INSERT INTO `administrator`(userId, adminLevel)
VALUES (@adminId, 5);

INSERT INTO `registeredUser`(userId, registrationStatus)
VALUES (@johnId, 'ACTIVE'),
       (@sarahId, 'ACTIVE'),
       (@mikeId, 'ACTIVE'),
       (@emmaId, 'ACTIVE'),
       (@davidId, 'ACTIVE'),
       (@lisaId, 'ACTIVE'),
       (@tomId, 'ACTIVE');


SET @bullsClub = UUID();
SET @sharksClub = UUID();
SET @stormersClub = UUID();
SET @lionsClub = UUID();
SET @cheetahsClub = UUID();
SET @pumasClub = UUID();

SET @leinsterClub = UUID();
SET @munsterClub = UUID();
SET @ulsterClub = UUID();
SET @connachtClub = UUID();

SET @cardiffClub = UUID();
SET @dragonsClub = UUID();
SET @ospreysClub = UUID();
SET @scarletsClub = UUID();

SET @glasgowWarriorsClub = UUID();
SET @edinburghClub = UUID();

SET @benettonClub = UUID();
SET @zebreParmaClub = UUID();

INSERT INTO `club`
(clubId, clubName, location, homeVenue)
VALUES
    (@bullsClub, 'Bulls', 'Pretoria', 'Loftus Versfeld Stadium'),
    (@sharksClub, 'Sharks', 'Durban', 'Hollywoodbets Kings Park Stadium'),
    (@stormersClub, 'Stormers', 'Cape Town', 'DHL Stadium'),
    (@lionsClub, 'Lions', 'Johannesburg', 'Emirates Airline Park'),

    (@leinsterClub, 'Leinster', 'Dublin', 'Aviva Stadium'),
    (@munsterClub, 'Munster', 'Limerick', 'Thomond Park'),
    (@ulsterClub, 'Ulster', 'Belfast', 'Kingspan Stadium'),
    (@connachtClub, 'Connacht', 'Galway', 'Dexcom Stadium'),

    (@cardiffClub, 'Cardiff', 'Cardiff', 'Cardiff Arms Park'),
    (@dragonsClub, 'Dragons', 'Newport', 'Rodney Parade'),
    (@ospreysClub, 'Ospreys', 'Swansea', 'Swansea.com Stadium'),
    (@scarletsClub, 'Scarlets', 'Llanelli', 'Parc y Scarlets'),

    (@glasgowWarriorsClub, 'Glasgow Warriors', 'Glasgow', 'Scotstoun Stadium'),
    (@edinburghClub, 'Edinburgh', 'Edinburgh', 'Hive Stadium'),

    (@benettonClub, 'Benetton', 'Treviso', 'Stadio Comunale di Monigo'),
    (@zebreParmaClub, 'Zebre Parma', 'Parma', 'Stadio Sergio Lanfranchi'),
    (@cheetahsClub, 'Cheetahs', 'Bloemfontein', 'Toyota Stadium'),
    (@pumasClub, 'Pumas', 'Nelspruit', 'Mbombela Stadium');


SET
@propId = UUID();
    SET
@hookerId = UUID();
    SET
@lockId = UUID();
    SET
@looseForwardId = UUID();
    SET
@scrumhalfId = UUID();
    SET
@flyhalfId = UUID();
    SET
@centreId = UUID();
    SET
@wingId = UUID();
    SET
@fullbackId = UUID();

INSERT INTO `position`
    (positionId, positionName, positionCategory, minRequired, maxAllowed)
VALUES (@propId, 'Prop', 'FORWARD', 2, 4),
       (@hookerId, 'Hooker', 'FORWARD', 1, 2),
       (@lockId, 'Lock', 'FORWARD', 2, 4),
       (@looseForwardId, 'Loose Forward', 'FORWARD', 3, 5),
       (@scrumhalfId, 'Scrum Half', 'BACK', 1, 2),
       (@flyhalfId, 'Fly Half', 'BACK', 1, 2),
       (@centreId, 'Centre', 'BACK', 2, 4),
       (@wingId, 'Wing', 'BACK', 2, 4),
       (@fullbackId, 'Fullback', 'BACK', 1, 2);

SET
@p1 = UUID();
    SET
@p2 = UUID();
    SET
@p3 = UUID();
    SET
@p4 = UUID();
    SET
@p5 = UUID();
    SET
@p6 = UUID();
    SET
@p7 = UUID();
    SET
@p8 = UUID();
    SET
@p9 = UUID();
    SET
@p10 = UUID();
    SET
@p11 = UUID();
    SET
@p12 = UUID();
    SET
@p13 = UUID();
    SET
@p14 = UUID();
    SET
@p15 = UUID();
    SET
@p16 = UUID();
    SET
@p17 = UUID();
    SET
@p18 = UUID();
    SET
@p19 = UUID();
    SET
@p20 = UUID();
    SET
@p21 = UUID();
    SET
@p22 = UUID();
    SET
@p23 = UUID();
    SET
@p24 = UUID();
    SET
@p25 = UUID();
    SET
@p26 = UUID();
    SET
@p27 = UUID();
    SET
@p28 = UUID();
    SET
@p29 = UUID();
    SET
@p30 = UUID();
    SET
@p31 = UUID();
    SET
@p32 = UUID();
    SET
@p33 = UUID();

INSERT INTO `player`
(playerId, clubId, positionId, playerName, value,
 attackingAbility, defensiveAbility, kickingAbility,
 discipline, consistency, fitness, currentForm)
VALUES (@p1, @bullsClub, @flyhalfId, 'Johan van Wyk', 12.5, 88, 72, 91, 80, 85, 90, 87),
       (@p2, @bullsClub, @wingId, 'Chris Botha', 10.5, 85, 70, 60, 78, 80, 88, 82),
       (@p3, @bullsClub, @centreId, 'Andre Jacobs', 11.0, 82, 84, 55, 85, 79, 86, 81),
       (@p4, @bullsClub, @lockId, 'Pieter Smith', 9.0, 60, 90, 20, 88, 82, 92, 79),
       (@p5, @bullsClub, @propId, 'Franco Adams', 8.0, 50, 92, 10, 87, 78, 90, 75),

       (@p6, @sharksClub, @flyhalfId, 'Ryan Williams', 13.0, 90, 73, 92, 81, 88, 90, 89),
       (@p7, @sharksClub, @centreId, 'Luke Daniels', 11.5, 84, 83, 50, 84, 80, 87, 83),
       (@p8, @sharksClub, @wingId, 'David Jacobs', 10.0, 88, 68, 45, 82, 77, 85, 80),
       (@p9, @sharksClub, @hookerId, 'Jason Brown', 8.5, 65, 89, 10, 88, 81, 90, 78),
       (@p10, @sharksClub, @looseForwardId, 'Grant White', 9.2, 72, 91, 20, 86, 84, 91, 82),

       (@p11, @stormersClub, @flyhalfId, 'Peter Adams', 13.5, 91, 76, 93, 85, 88, 92, 90),
       (@p12, @stormersClub, @centreId, 'Mark Taylor', 11.0, 82, 82, 50, 84, 80, 88, 81),
       (@p13, @stormersClub, @wingId, 'Kyle Petersen', 10.8, 89, 67, 40, 82, 79, 89, 84),
       (@p14, @stormersClub, @lockId, 'Dean Miller', 8.9, 58, 93, 10, 87, 82, 91, 80),
       (@p15, @stormersClub, @fullbackId, 'Neil Thomas', 11.2, 85, 74, 80, 84, 83, 88, 85),

       (@p16, @lionsClub, @flyhalfId, 'Morne Venter', 12.0, 84, 71, 89, 80, 82, 86, 81),
       (@p17, @lionsClub, @centreId, 'Ruan Smith', 10.5, 81, 80, 40, 84, 79, 85, 79),
       (@p18, @lionsClub, @wingId, 'Jaco Meyer', 10.1, 86, 66, 30, 80, 77, 87, 80),
       (@p19, @lionsClub, @looseForwardId, 'Willem Botha', 9.0, 68, 90, 10, 86, 80, 90, 78),
       (@p20, @lionsClub, @propId, 'Hendrik Fourie', 8.1, 52, 92, 5, 88, 81, 91, 76),

       (@p21, @cheetahsClub, @flyhalfId, 'Stefan Ross', 11.5, 82, 69, 85, 79, 80, 84, 78),
       (@p22, @cheetahsClub, @centreId, 'Chris Nel', 10.2, 80, 78, 40, 82, 78, 84, 77),
       (@p23, @cheetahsClub, @wingId, 'Brandon Visser', 9.8, 84, 65, 20, 80, 77, 85, 76),
       (@p24, @cheetahsClub, @hookerId, 'Paul Kruger', 8.0, 60, 87, 10, 86, 80, 89, 75),
       (@p25, @cheetahsClub, @lockId, 'Jacques Swanepoel', 8.4, 55, 89, 5, 87, 81, 90, 76),

       (@p26, @pumasClub, @flyhalfId, 'Kevin Roberts', 11.0, 80, 68, 82, 78, 79, 83, 75),
       (@p27, @pumasClub, @centreId, 'Sean Peters', 9.9, 78, 76, 35, 80, 77, 84, 74),
       (@p28, @pumasClub, @wingId, 'Alan Brooks', 9.5, 82, 64, 15, 79, 76, 85, 73),
       (@p29, @pumasClub, @looseForwardId, 'Dylan Green', 8.6, 66, 88, 10, 84, 80, 89, 75),
       (@p30, @pumasClub, @fullbackId, 'Ethan Lewis', 10.4, 81, 72, 78, 82, 79, 86, 77),

       (@p31, @bullsClub, @propId, 'Sibusiso Dlamini', 8.3, 51, 90, 8, 86, 80, 89, 77),
       (@p32, @sharksClub, @scrumhalfId, 'Thabo Mokoena', 10.7, 83, 75, 72, 84, 82, 88, 84),
       (@p33, @stormersClub, @scrumhalfId, 'Daniel van Zyl', 10.3, 81, 74, 70, 85, 83, 87, 82);

/*
    Injury and suspension are demonstrated on players nobody has selected
    (@p18 injured, @p23 suspended). SquadValidationService rejects a squad
    containing an unavailable player, so keeping the selected players
    available means every seeded squad can still be edited in the app.
*/
INSERT INTO `playerAvailability`
    (availabilityId, playerId, status, effectiveDate)
VALUES (UUID(), @p1, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p2, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p3, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p4, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p5, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p6, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p7, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p8, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p9, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p10, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p11, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p12, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p13, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p14, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p15, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p16, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p17, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p18, 'INJURED', CURRENT_DATE()),
       (UUID(), @p19, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p20, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p21, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p22, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p23, 'SUSPENDED', CURRENT_DATE()),
       (UUID(), @p24, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p25, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p26, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p27, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p28, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p29, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p30, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p31, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p32, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p33, 'ACTIVE', CURRENT_DATE());

SET
@team1 = UUID();
    SET
@team2 = UUID();
    SET
@team3 = UUID();
    SET
@team4 = UUID();
    SET
@team5 = UUID();

INSERT INTO `fantasyTeam`
(teamId,
 owner_user_id,
 teamName,
 remainingBudget,
 isValid)
VALUES (@team1, @johnId, 'John Warriors', 5.00, TRUE),
       (@team2, @sarahId, 'Sarah Sharks', 7.50, TRUE),
       (@team3, @mikeId, 'Mike Titans', 10.00, TRUE),
       (@team4, @emmaId, 'Emma Eagles', 6.00, TRUE),
       (@team5, @davidId, 'David Dragons', 11.00, TRUE);

INSERT INTO `team_player_selection`
    (selectionId, teamId, playerId, squadRole, isCaptain, is_vice_captain)
SELECT UUID(),
       teams.teamId,
       players.playerId,
       CASE
           WHEN players.playerId IN (@p31, @p24, @p25, @p33, @p6) THEN 'BENCH'
           ELSE 'STARTING'
           END,
       players.playerId = @p1,
       players.playerId = @p3
FROM (SELECT @team1 AS teamId
      UNION ALL
      SELECT @team2
      UNION ALL
      SELECT @team3
      UNION ALL
      SELECT @team4
      UNION ALL
      SELECT @team5) AS teams
         CROSS JOIN (SELECT @p5 AS playerId
                     UNION ALL
                     SELECT @p20
                     UNION ALL
                     SELECT @p31
                     UNION ALL
                     SELECT @p9
                     UNION ALL
                     SELECT @p24
                     UNION ALL
                     SELECT @p4
                     UNION ALL
                     SELECT @p14
                     UNION ALL
                     SELECT @p25
                     UNION ALL
                     SELECT @p10
                     UNION ALL
                     SELECT @p19
                     UNION ALL
                     SELECT @p29
                     UNION ALL
                     SELECT @p32
                     UNION ALL
                     SELECT @p33
                     UNION ALL
                     SELECT @p1
                     UNION ALL
                     SELECT @p6
                     UNION ALL
                     SELECT @p3
                     UNION ALL
                     SELECT @p7
                     UNION ALL
                     SELECT @p2
                     UNION ALL
                     SELECT @p8
                     UNION ALL
                     SELECT @p15) AS players;



/* Functional recommendation data: current player is in Team 1's editable squad; recommended player is not. */
SET @recommendation1 = UUID();

INSERT INTO `playerRecommendation`
    (recommendationId, teamId, current_player_id, recommended_player_id, reason, score, isDismissed)
VALUES (@recommendation1,
        @team1,
        @p1,
        @p21,
        'Lower-cost fly-half alternative for transfer-planning tests',
        8.25,
        FALSE);

SET
@publicLeague = UUID();
    SET
@privateLeague = UUID();

INSERT INTO `league`
(leagueId,
 manager_user_id,
 leagueName,
 description,
 leagueType,
 maxMembers)
VALUES (@publicLeague,
        NULL,
        'Global Fantasy Rugby',
        'Official public league for all players',
        'PUBLIC',
        100);

INSERT INTO `league`
(leagueId,
 manager_user_id,
 leagueName,
 description,
 leagueType,
 leagueCode,
 maxMembers)
VALUES (@privateLeague,
        NULL,
        'Friends Rugby League',
        'Private code-entry league',
        'PRIVATE',
        'ABC123',
        20);


INSERT INTO `leagueMembership`
(membershipId,
 leagueId,
 registered_user_id,
 teamId)
VALUES (UUID(), @publicLeague, @johnId, @team1),
       (UUID(), @publicLeague, @sarahId, @team2),
       (UUID(), @publicLeague, @mikeId, @team3),
       (UUID(), @publicLeague, @emmaId, @team4),
       (UUID(), @publicLeague, @davidId, @team5),

       (UUID(), @privateLeague, @sarahId, @team2),
       (UUID(), @privateLeague, @johnId, @team1),
       (UUID(), @privateLeague, @mikeId, @team3);

/* Only a PRIVATE league has a manager. A public league is run by the
   administrators, so manager_user_id stays NULL on Global Fantasy Rugby. */
UPDATE `league`
SET manager_user_id = @sarahId
WHERE leagueId = @privateLeague;

SET
@round1 = UUID();
SET
@round2 = UUID();

INSERT INTO `fantasyRound`
    (roundId, season, roundNumber, openDate, lockDeadline, endDate, status)
VALUES (@round1, '2026', 1, '2026-07-01 00:00:00', '2026-07-07 23:59:59', '2026-07-14 23:59:59', 'LOCKED'),
       (@round2, '2026', 2, '2026-07-15 00:00:00', '2026-07-21 23:59:59', '2026-07-28 23:59:59', 'OPEN');



/* Pending transfer request for the open round. It does not alter the editable squad until confirmed by application logic. */
SET @transfer1 = UUID();

INSERT INTO `transfer`
    (transferId, teamId, roundId, removed_player_id, added_player_id,
     removed_player_value, added_player_value, penaltyPoints, status, confirmedAt, created_by_user_id)
VALUES (@transfer1,
        @team1,
        @round2,
        @p1,
        @p21,
        12.50,
        11.50,
        0,
        'PENDING',
        NULL,
        @johnId);

INSERT INTO `fantasy_team_round_selection`
(selectionId, roundId, teamId, playerId, squadRole, isCaptain, is_vice_captain)
SELECT UUID(),
       @round1,
       currentSelection.teamId,
       currentSelection.playerId,
       currentSelection.squadRole,
       currentSelection.isCaptain,
       currentSelection.is_vice_captain
FROM `team_player_selection` AS currentSelection
WHERE currentSelection.teamId IN (@team1, @team2, @team3, @team4, @team5);

INSERT INTO `roundLock`
    (lockId, roundId, lockAction, action_by_admin_user_id, reason)
VALUES (UUID(), @round1, 'LOCKED', @adminId, 'Round 1 locked for seed simulation data');

/* Round 1 is left IN_PROGRESS (not COMPLETED) so its already-simulated fixtures
   remain resimulatable during the presentation. ControlledResimulationServiceImpl
   only permits resimulation while the round is LOCKED or IN_PROGRESS. */
UPDATE `fantasyRound`
SET status = 'IN_PROGRESS'
WHERE roundId = @round1;

SET
@fixture1 = UUID();
    SET
@fixture2 = UUID();
    SET
@fixture3 = UUID();

INSERT INTO `fixture`
(fixtureId,
 leagueId,
 roundId,
 team_a_id,
 team_b_id,
 fixtureDate,
 fixtureTime,
 status,
 simulationDate)
VALUES (@fixture1,
        @publicLeague,
        @round1,
        @team1,
        @team2,
        '2026-07-08',
        '15:00:00',
        'COMPLETED',
        '2026-07-08 16:30:00'),

       (@fixture2,
        @publicLeague,
        @round1,
        @team3,
        @team4,
        '2026-07-08',
        '18:00:00',
        'COMPLETED',
        '2026-07-08 19:30:00'),

       (@fixture3,
        @privateLeague,
        @round1,
        @team2,
        @team1,
        '2026-07-09',
        '16:00:00',
        'COMPLETED',
        '2026-07-09 17:30:00');

SET
@ruleTry = UUID();
    SET
@ruleAssist = UUID();
    SET
@ruleTackle = UUID();
    SET
@ruleConversion = UUID();
    SET
@ruleMissedTackle = UUID();
    SET
@ruleYellowCard = UUID();
    SET
@ruleRedCard = UUID();

INSERT INTO `scoringRule`
(ruleId,
 season,
 eventType,
 pointsAwarded,
 isDeduction,
 description)
VALUES (@ruleTry,
        '2026',
        'TRY',
        5,
        FALSE,
        'Points awarded for scoring a try'),

       (@ruleAssist,
        '2026',
        'ASSIST',
        3,
        FALSE,
        'Points awarded for a try assist'),

       (@ruleTackle,
        '2026',
        'TACKLE',
        1,
        FALSE,
        'Points awarded for a successful tackle'),

       (@ruleConversion,
        '2026',
        'CONVERSION',
        2,
        FALSE,
        'Points awarded for a successful conversion'),

       (@ruleMissedTackle,
        '2026',
        'MISSED_TACKLE',
        1,
        TRUE,
        'Deduction for a missed tackle'),

       (@ruleYellowCard,
        '2026',
        'YELLOW_CARD',
        3,
        TRUE,
        'Deduction for a yellow card'),

       (@ruleRedCard,
        '2026',
        'RED_CARD',
        10,
        TRUE,
        'Deduction for a red card');

SET
@masterLeaderboard = UUID();
    SET
@publicLeaderboard = UUID();
    SET
@privateLeaderboard = UUID();

INSERT INTO `leaderboard`
(leaderboardId,
 leagueId,
 season,
 scope)
VALUES (@masterLeaderboard,
        NULL,
        '2026',
        'MASTER'),
       (@publicLeaderboard,
        @publicLeague,
        '2026',
        'LEAGUE'),
       (@privateLeaderboard,
        @privateLeague,
        '2026',
        'LEAGUE');

INSERT INTO `ranking`
(rankingId,
 leaderboardId,
 teamId,
 currentRanking,
 previousRanking,
 matchesPlayed,
 matchesWon,
 matchesDrawn,
 matchesLost,
 pointsFor,
 pointsAgainst,
 leaguePoints,
 total_fantasy_points)
/* MASTER-scope rows below are DERIVED from a live LeaderboardServiceImpl / LeaderboardAggregator
   recompute against this exact seed (LEAGUE-scope PRIVATE results are excluded from MASTER, per
   the public/private leaderboard rule) - regenerate rather than hand-edit if the underlying
   fixtures/matchResults change. Sarah Sharks ranks last (16) despite 14 fantasy points because the
   comparator sorts on leaguePoints then score difference; hers is -1, worse than an unplayed
   team's 0 - this is correct, not a bug. Positions 5-15 are reserved for the 11 teams the
   additive-league seed later registers on this leaderboard (see the block below querying
   @masterMax); because those teams are inserted after this one and their positions are computed
   relative to the running MAX(currentRanking), placing Sarah at 16 here means that block will
   actually assign them 17-27 rather than 5-15 - the zero/last-place semantics still hold, but the
   literal position numbers will not match a fresh live recompute exactly unless that block is
   also restructured to run before Sarah's row is inserted. */
VALUES (UUID(), @masterLeaderboard, @team1, 1, NULL, 1, 1, 0, 0, 15, 14, 4, 15),
       (UUID(), @masterLeaderboard, @team3, 2, NULL, 1, 0, 1, 0, 14, 14, 2, 14),
       (UUID(), @masterLeaderboard, @team4, 3, NULL, 1, 0, 1, 0, 14, 14, 2, 14),
       (UUID(), @masterLeaderboard, @team5, 4, NULL, 0, 0, 0, 0, 0, 0, 0, 0),
       (UUID(), @masterLeaderboard, @team2, 16, NULL, 1, 0, 0, 1, 14, 15, 0, 14),

       (UUID(), @publicLeaderboard, @team1, 1, NULL, 1, 1, 0, 0, 15, 14, 4, 15),
       (UUID(), @publicLeaderboard, @team3, 2, NULL, 1, 0, 1, 0, 14, 14, 2, 14),
       (UUID(), @publicLeaderboard, @team4, 3, NULL, 1, 0, 1, 0, 14, 14, 2, 14),
       (UUID(), @publicLeaderboard, @team5, 4, NULL, 0, 0, 0, 0, 0, 0, 0, 0),
       (UUID(), @publicLeaderboard, @team2, 5, NULL, 1, 0, 0, 1, 14, 15, 0, 14),

       (UUID(), @privateLeaderboard, @team2, 1, NULL, 1, 1, 0, 0, 18, 15, 4, 18),
       (UUID(), @privateLeaderboard, @team3, 2, NULL, 0, 0, 0, 0, 0, 0, 0, 0),
       (UUID(), @privateLeaderboard, @team1, 3, NULL, 1, 0, 0, 1, 15, 18, 0, 15);

INSERT INTO `notification`
(notificationId,
 userId,
 type,
 body,
 related_entity_type,
 related_entity_id)
VALUES (UUID(),
        @johnId,
        'LEADERBOARD_CHANGE',
        'Your team moved to rank 1.',
        'LEADERBOARD',
        @publicLeaderboard),

       (UUID(),
        @sarahId,
        'ROUND_LOCK',
        'Round 1 has been locked.',
        'ROUND',
        @round1),

       (UUID(),
        @mikeId,
        'POINTS_UPDATE',
        'Weekly points have been updated.',
        'TEAM',
        @team3),

       (UUID(),
        @emmaId,
        'SIMULATED_RESULT',
        'Fixture simulation completed.',
        'FIXTURE',
        @fixture1);

SET
@simulationSettingsId = UUID();

INSERT INTO `simulationSettings`
(settingsId,
 season,
 settingsVersion,
 player_ability_weight,
 player_form_weight,
 team_balance_weight,
 random_variation_weight,
 require_admin_approval,
 allowResimulation,
 maxResimulations,
 isActive)
/* require_admin_approval is FALSE so simulated results are usable and visible
   without an admin approval step, and allowResimulation is TRUE (up to 3 runs)
   so fixtures can be resimulated live during the presentation. */
VALUES (@simulationSettingsId,
        '2026',
        1,
        35.00,
        25.00,
        20.00,
        20.00,
        FALSE,
        TRUE,
        3,
        TRUE);


SET
@result1 = UUID();
SET
@result2 = UUID();
SET
@result3 = UUID();

INSERT INTO `matchResult`
(resultId,
 fixtureId,
 settingsId,
 team_a_score,
 team_b_score,
 winnerSide,
 isDraw,
 approved,
 simulation_run_number)
/* Scores are the fantasy-point totals of each side's selected players, matching the
   match_team_score breakdown rows inserted below. winnerSide/isDraw are unchanged:
   15 > 14 and 18 > 15 still resolve to TEAM_A, and 14 = 14 is still a draw. */
VALUES (@result1,
        @fixture1,
        @simulationSettingsId,
        15,
        14,
        'TEAM_A',
        FALSE,
        FALSE,
        1),
       (@result2,
        @fixture2,
        @simulationSettingsId,
        14,
        14,
        NULL,
        TRUE,
        FALSE,
        1),
       (@result3,
        @fixture3,
        @simulationSettingsId,
        18,
        15,
        'TEAM_A',
        FALSE,
        FALSE,
        1);

INSERT INTO `match_team_score`
(scoreId, resultId, teamId, teamSide, playerPoints, captainBonus, transferPenalty)
/* playerPoints must equal the sum of the seeded fantasyPoints.totalPoints for that team's
   selected players, because trg_match_team_score_insert requires
   playerPoints + captainBonus - transferPenalty to equal the stored matchResult score.
   captainBonus is 0 to mirror TeamScoreServiceImpl, which currently always writes 0. */
VALUES (UUID(), @result1, @team1, 'TEAM_A', 15, 0, 0),
       (UUID(), @result1, @team2, 'TEAM_B', 14, 0, 0),
       (UUID(), @result2, @team3, 'TEAM_A', 14, 0, 0),
       (UUID(), @result2, @team4, 'TEAM_B', 14, 0, 0),
       (UUID(), @result3, @team2, 'TEAM_A', 18, 0, 0),
       (UUID(), @result3, @team1, 'TEAM_B', 15, 0, 0);

/* Results are deliberately LEFT UNAPPROVED (approved = FALSE). An approved match
   result cannot be resimulated (ControlledResimulationServiceImpl), so keeping
   every simulated result unapproved lets the presenter demonstrate resimulation
   on any of these fixtures. With require_admin_approval = FALSE on the active
   settings, unapproved results still display and count. */

SET
@stat1 = UUID();
SET
@stat2 = UUID();
SET
@stat3 = UUID();
SET
@stat4 = UUID();
SET
@stat5 = UUID();
SET
@stat6 = UUID();

INSERT INTO `playerStatistics`
(statId,
 resultId,
 teamId,
 playerId,
 tries,
 assists,
 tackles,
 conversions,
 metersGained)
VALUES (@stat1,
        @result1,
        @team1,
        @p1,
        1,
        1,
        7,
        0,
        120),

       (@stat2,
        @result1,
        @team2,
        @p6,
        2,
        0,
        4,
        0,
        150),

       (@stat3,
        @result2,
        @team3,
        @p3,
        1,
        1,
        6,
        0,
        95),

       (@stat4,
        @result2,
        @team4,
        @p7,
        0,
        2,
        8,
        0,
        88),

       (@stat5,
        @result3,
        @team2,
        @p8,
        2,
        1,
        5,
        0,
        142),

       (@stat6,
        @result3,
        @team1,
        @p15,
        1,
        0,
        6,
        2,
        110);

SET
@points1 = UUID();
SET
@points2 = UUID();
SET
@points3 = UUID();
SET
@points4 = UUID();
SET
@points5 = UUID();
SET
@points6 = UUID();

INSERT INTO `fantasyPoints`
(pointsId,
 statId,
 totalPoints,
 calculationVersion,
 isFinal)
VALUES (@points1, @stat1, 15, 1, TRUE),
       (@points2, @stat2, 14, 1, TRUE),
       (@points3, @stat3, 14, 1, TRUE),
       (@points4, @stat4, 14, 1, TRUE),
       (@points5, @stat5, 18, 1, TRUE),
       (@points6, @stat6, 15, 1, TRUE);

INSERT INTO `fantasy_point_breakdown`
    (breakdownId, pointsId, ruleId, eventCount, pointsEarned)
VALUES (UUID(), @points1, @ruleTry, 1, 5),
       (UUID(), @points1, @ruleAssist, 1, 3),
       (UUID(), @points1, @ruleTackle, 7, 7),
       (UUID(), @points2, @ruleTry, 2, 10),
       (UUID(), @points2, @ruleTackle, 4, 4),
       (UUID(), @points3, @ruleTry, 1, 5),
       (UUID(), @points3, @ruleAssist, 1, 3),
       (UUID(), @points3, @ruleTackle, 6, 6),
       (UUID(), @points4, @ruleAssist, 2, 6),
       (UUID(), @points4, @ruleTackle, 8, 8),
       (UUID(), @points5, @ruleTry, 2, 10),
       (UUID(), @points5, @ruleAssist, 1, 3),
       (UUID(), @points5, @ruleTackle, 5, 5),
       (UUID(), @points6, @ruleTry, 1, 5),
       (UUID(), @points6, @ruleConversion, 2, 4),
       (UUID(), @points6, @ruleTackle, 6, 6);

INSERT INTO `log`
(logId,
 userId,
 entityType,
 entityId,
 actionType,
 description)
VALUES (UUID(),
        @johnId,
        'LEAGUE',
        @publicLeague,
        'CREATE',
        'Created public league'),

       (UUID(),
        @sarahId,
        'LEAGUE',
        @privateLeague,
        'CREATE',
        'Created private league');

COMMIT;

/* ============================================================================
   SECTION 2 -- Extra available players (from seed-extra-players.sql), rewired
   to the position/club variables generated by Section 1 rather than the
   hard-coded UUIDs the standalone file used.
   ============================================================================ */
START TRANSACTION;

SET @xClub   = @bullsClub;
SET @xLoose  = @looseForwardId;
SET @xHooker = @hookerId;
SET @xLock   = @lockId;

SET @lf1 = UUID();
SET @lf2 = UUID();
SET @hk1 = UUID();
SET @lk1 = UUID();

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (@lf1, @xClub, @xLoose,  'Tank Coetzee', 8.80, 78, 84, 30, 80, 79, 85, 80, 1),
    (@lf2, @xClub, @xLoose,  'Bandi Nkosi',  9.10, 80, 82, 35, 78, 80, 86, 82, 1),
    (@hk1, @xClub, @xHooker, 'Wian du Toit', 8.40, 66, 86, 20, 85, 81, 88, 79, 1),
    (@lk1, @xClub, @xLock,   'Ruben Steyn',  8.70, 58, 90, 15, 87, 82, 90, 78, 1);

INSERT INTO `playerAvailability`
    (availabilityId, playerId, status, effectiveDate, endDate, notes)
VALUES
    (UUID(), @lf1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available loose forward'),
    (UUID(), @lf2, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available loose forward'),
    (UUID(), @hk1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available hooker'),
    (UUID(), @lk1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available lock');

COMMIT;


/* ============================================================================
   SECTION 3 -- Presentation accounts
     3 admins   (Admin@123)
     3 users    (User@123)
     8 named    (Trytons@123): Christan, Lindsay, Jaunte, Magdeli, Sameer,
                Timothy, Jarryd, Sulaimaan
   Named + demo users each get an EMPTY fantasy team (full budget, isValid=FALSE)
   so squads can be built live during the presentation.
   ============================================================================ */
START TRANSACTION;

SET @uAdmin1 = UUID();
SET @uAdmin2 = UUID();
SET @uAdmin3 = UUID();
SET @uUser1  = UUID();
SET @uUser2  = UUID();
SET @uUser3  = UUID();
SET @uChristan  = UUID();
SET @uLindsay   = UUID();
SET @uJaunte    = UUID();
SET @uMagdeli   = UUID();
SET @uSameer    = UUID();
SET @uTimothy   = UUID();
SET @uJarryd    = UUID();
SET @uSulaimaan = UUID();

INSERT INTO `user` (userId, email, passwordHash, username, role)
VALUES
    (@uAdmin1, 'admin1@trytons.com', '$2a$12$d1rLiJtFwXVY41UexdTOSuUwZRSHiwDdclhkhX3GGpbq/iWJ4kLxC', 'admin1', 'ADMINISTRATOR'),
    (@uAdmin2, 'admin2@trytons.com', '$2a$12$iTXPHdQFXV4YoOAZ21dl1uay0qfBSnMthWdFa2jNL6VK.OEAKikPS', 'admin2', 'ADMINISTRATOR'),
    (@uAdmin3, 'admin3@trytons.com', '$2a$12$DMwyEbVXwxgRlR1oih/iZO2mXLboiXZt91jHbnAa1mfGfX7ZSjdEW', 'admin3', 'ADMINISTRATOR'),
    (@uUser1,  'user1@trytons.com',  '$2a$12$bbay0zN.BP.OdYkPa.cwYeaYuXO91lzSVQVq8Q4swhGM2n3QKPQ9W', 'user1',  'REGISTERED_USER'),
    (@uUser2,  'user2@trytons.com',  '$2a$12$niqsE/yVx/sQ.Fn8yAHi2ufVGs9YlqQe3YMmGxFYGzsDpJ3GaaVgK', 'user2',  'REGISTERED_USER'),
    (@uUser3,  'user3@trytons.com',  '$2a$12$.nqlXIH5jbJWgaYqx8.g6.qy4D0vySmAr6qaX2nVoOyhUnYm3kC7q', 'user3',  'REGISTERED_USER'),
    (@uChristan,  'christan@trytons.com',  '$2a$12$ah2A/BnjiRpjRV3Mp5pE..tBhhDJ.9jzdh.dECWiJ1KCel5Sqq7ji', 'christan',  'REGISTERED_USER'),
    (@uLindsay,   'lindsay@trytons.com',   '$2a$12$HNbLLapT8.jVEvALlw.SKeCAREsuOzXQTwf23bvx.OwV4ymo4eju6', 'lindsay',   'REGISTERED_USER'),
    (@uJaunte,    'jaunte@trytons.com',    '$2a$12$xIHlmX37cxWEkT19TJFQeu6AyREPxoxOrzd6wsyGKBwKNFBH/Odca', 'jaunte',    'REGISTERED_USER'),
    (@uMagdeli,   'magdeli@trytons.com',   '$2a$12$TxqAjtuPv0PdFKkohzM1weBsxDBP3rg7iEqcHNn66ilS4Fy86Tlne', 'magdeli',   'REGISTERED_USER'),
    (@uSameer,    'sameer@trytons.com',    '$2a$12$oqy2BFf3jEzoQmffOA5kEuidx3U2yOi1At3UTbMU2ZVnFdXZwVzjm', 'sameer',    'REGISTERED_USER'),
    (@uTimothy,   'timothy@trytons.com',   '$2a$12$Y0xamAnRzJICEnRU7P/pHeol1pkiAzs4t9AuL6bByzz0WtpNJYTqO', 'timothy',   'REGISTERED_USER'),
    (@uJarryd,    'jarryd@trytons.com',    '$2a$12$cDA7q9mezv2poohpXj7lrOxVnrjZQq20djvrKw9fxjr35Lb/YZWNK', 'jarryd',    'REGISTERED_USER'),
    (@uSulaimaan, 'sulaimaan@trytons.com', '$2a$12$nkH05WodTmzrPTlpBD2D1.vnTwwRumIYXWCM4ANmKLf78kr9rfgm2', 'sulaimaan', 'REGISTERED_USER');

INSERT INTO `administrator` (userId, adminLevel)
VALUES (@uAdmin1, 5), (@uAdmin2, 3), (@uAdmin3, 1);

INSERT INTO `registeredUser` (userId, registrationStatus)
VALUES
    (@uUser1, 'ACTIVE'), (@uUser2, 'ACTIVE'), (@uUser3, 'ACTIVE'),
    (@uChristan, 'ACTIVE'), (@uLindsay, 'ACTIVE'), (@uJaunte, 'ACTIVE'),
    (@uMagdeli, 'ACTIVE'), (@uSameer, 'ACTIVE'), (@uTimothy, 'ACTIVE'),
    (@uJarryd, 'ACTIVE'), (@uSulaimaan, 'ACTIVE');

SET @tUser1  = UUID();
SET @tUser2  = UUID();
SET @tUser3  = UUID();
SET @tChristan  = UUID();
SET @tLindsay   = UUID();
SET @tJaunte    = UUID();
SET @tMagdeli   = UUID();
SET @tSameer    = UUID();
SET @tTimothy   = UUID();
SET @tJarryd    = UUID();
SET @tSulaimaan = UUID();

INSERT INTO `fantasyTeam` (teamId, owner_user_id, teamName, remainingBudget, isValid)
VALUES
    (@tUser1,  @uUser1,  'Wanderers XV',   196.00, FALSE),
    (@tUser2,  @uUser2,  'Kingsmead XV',   196.00, FALSE),
    (@tUser3,  @uUser3,  'Ellis Park XV', 196.00, FALSE),
    (@tChristan,  @uChristan,  'Christan XV',  196.00, FALSE),
    (@tLindsay,   @uLindsay,   'Lindsay XV',   196.00, FALSE),
    (@tJaunte,    @uJaunte,    'Jaunte XV',    196.00, FALSE),
    (@tMagdeli,   @uMagdeli,   'Magdeli XV',   196.00, FALSE),
    (@tSameer,    @uSameer,    'Sameer XV',    196.00, FALSE),
    (@tTimothy,   @uTimothy,   'Timothy XV',   196.00, FALSE),
    (@tJarryd,    @uJarryd,    'Jarryd XV',    196.00, FALSE),
    (@tSulaimaan, @uSulaimaan, 'Sulaimaan XV', 196.00, FALSE);

COMMIT;

/* ----------------------------------------------------------------------------
   Active Users Report snapshot -- recorded here, once every presentation
   account above exists, so it captures the real active-user list. (It was
   previously inserted right after Section 1's ~10 users with no resultJson
   at all, which rendered as "This report contains no data" even though the
   ACTIVE_USERS report logic itself -- SystemReportServiceImpl.buildActiveUsersReport()
   / UserDAOImpl.getActiveUsers() -- works correctly.) The JSON shape here
   mirrors buildActiveUsersReport() exactly: {"activeUserCount", "users":[{
   "userId","username","role"}]}, with an empty-object parametersJson to match
   what generateReport() stores when no parameters are supplied.
   ---------------------------------------------------------------------------- */
START TRANSACTION;

INSERT INTO `systemReport`
(reportId, generated_by_admin_user_id, reportType, reportTitle, parametersJson, resultJson)
SELECT
    UUID(),
    @adminId,
    'ACTIVE_USERS',
    'Active Users Report',
    JSON_OBJECT(),
    JSON_OBJECT(
        'activeUserCount', COUNT(*),
        'users', JSON_ARRAYAGG(JSON_OBJECT('userId', userId, 'username', username, 'role', role))
    )
FROM `user`
WHERE isActive = TRUE;

COMMIT;


/* ============================================================================
   SECTION 4 -- Presentation leagues, fantasy rounds and fixtures
   ----------------------------------------------------------------------------
   Rounds reuse season '2026' (base seed used round numbers 1-2, so these
   start at 3). All three leagues are PUBLIC (leagueCode must be NULL).

     Showcase league : rounds 3-10  open 24/07 11:00, lock 12:30, end 14:00
                       round 11 is the special one, lock 11:45
                       full 8-team round-robin -> 4 fixtures per round
     Sunrise league  : round 12  open 24/07 08:00, lock 09:45, end 10:00
     Test league     : round 13  open 23/07 12:00, lock 23:00, end 24/07 00:00
   ============================================================================ */
START TRANSACTION;

SET @rA3  = UUID();
SET @rA4  = UUID();
SET @rA5  = UUID();
SET @rA6  = UUID();
SET @rA7  = UUID();
SET @rA8  = UUID();
SET @rA9  = UUID();
SET @rA10 = UUID();
SET @rA11 = UUID();
SET @rB   = UUID();
SET @rT   = UUID();

INSERT INTO `fantasyRound`
    (roundId, season, roundNumber, openDate, lockDeadline, endDate, status)
VALUES
    (@rA3,  '2026',  3, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'OPEN'),
    (@rA4,  '2026',  4, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA5,  '2026',  5, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA6,  '2026',  6, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA7,  '2026',  7, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA8,  '2026',  8, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA9,  '2026',  9, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA10, '2026', 10, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA11, '2026', 11, '2026-07-24 11:00:00', '2026-07-24 11:45:00', '2026-07-24 14:00:00', 'OPEN'),
    (@rB,   '2026', 12, '2026-07-24 08:00:00', '2026-07-24 09:45:00', '2026-07-24 10:00:00', 'OPEN'),
    (@rT,   '2026', 13, '2026-07-23 12:00:00', '2026-07-23 23:00:00', '2026-07-24 00:00:00', 'OPEN');

SET @lgShowcase = UUID();
SET @lgSunrise  = UUID();
SET @lgTest     = UUID();

INSERT INTO `league`
    (leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, maxMembers)
VALUES
    (@lgShowcase, NULL, 'Fantasy TryTons Showcase',
     'Main presentation league. Eight-team round-robin across rounds 3-11.',
     'PUBLIC', NULL, 50),
    (@lgSunrise, NULL, 'TryTons Sunrise Sevens',
     'Fun quick-fire sunrise league for the group. One morning round.',
     'PUBLIC', NULL, 30),
    (@lgTest, NULL, 'Cape Town Classic',
     'Open eight-team league for new managers finding their feet.',
     'PUBLIC', NULL, 30);

/* Memberships. Showcase carries the 8 named players plus the 3 demo users;
   Sunrise and Test carry the 8 named players. */
INSERT INTO `leagueMembership` (membershipId, leagueId, registered_user_id, teamId)
VALUES
    (UUID(), @lgShowcase, @uChristan,  @tChristan),
    (UUID(), @lgShowcase, @uLindsay,   @tLindsay),
    (UUID(), @lgShowcase, @uJaunte,    @tJaunte),
    (UUID(), @lgShowcase, @uMagdeli,   @tMagdeli),
    (UUID(), @lgShowcase, @uSameer,    @tSameer),
    (UUID(), @lgShowcase, @uTimothy,   @tTimothy),
    (UUID(), @lgShowcase, @uJarryd,    @tJarryd),
    (UUID(), @lgShowcase, @uSulaimaan, @tSulaimaan),
    (UUID(), @lgShowcase, @uUser1,     @tUser1),
    (UUID(), @lgShowcase, @uUser2,     @tUser2),
    (UUID(), @lgShowcase, @uUser3,     @tUser3),

    (UUID(), @lgSunrise, @uChristan,  @tChristan),
    (UUID(), @lgSunrise, @uLindsay,   @tLindsay),
    (UUID(), @lgSunrise, @uJaunte,    @tJaunte),
    (UUID(), @lgSunrise, @uMagdeli,   @tMagdeli),
    (UUID(), @lgSunrise, @uSameer,    @tSameer),
    (UUID(), @lgSunrise, @uTimothy,   @tTimothy),
    (UUID(), @lgSunrise, @uJarryd,    @tJarryd),
    (UUID(), @lgSunrise, @uSulaimaan, @tSulaimaan),

    (UUID(), @lgTest, @uChristan,  @tChristan),
    (UUID(), @lgTest, @uLindsay,   @tLindsay),
    (UUID(), @lgTest, @uJaunte,    @tJaunte),
    (UUID(), @lgTest, @uMagdeli,   @tMagdeli),
    (UUID(), @lgTest, @uSameer,    @tSameer),
    (UUID(), @lgTest, @uTimothy,   @tTimothy),
    (UUID(), @lgTest, @uJarryd,    @tJarryd),
    (UUID(), @lgTest, @uSulaimaan, @tSulaimaan);

/* Fantasy TryTons Showcase, TryTons Sunrise Sevens and Cape Town Classic are
   all PUBLIC, so none of them gets a manager: public leagues are run by the
   administrators and have no manager who plays in them. (Managers can only be
   assigned once the membership exists -- trg_league_manager_update -- which is
   why any private-league assignment has to come after its memberships.) */

/* Showcase fixtures: 8-team round-robin.
   T1=Christan T2=Lindsay T3=Jaunte T4=Magdeli T5=Sameer T6=Timothy T7=Jarryd T8=Sulaimaan */
INSERT INTO `fixture`
    (fixtureId, leagueId, roundId, team_a_id, team_b_id, fixtureDate, fixtureTime, status)
VALUES
    (UUID(), @lgShowcase, @rA3, @tChristan,  @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA3, @tLindsay,   @tJarryd,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA3, @tJaunte,    @tTimothy,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA3, @tMagdeli,   @tSameer,    '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA4, @tLindsay,   @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA4, @tJaunte,    @tChristan,  '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA4, @tMagdeli,   @tJarryd,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA4, @tSameer,    @tTimothy,   '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA5, @tJaunte,    @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA5, @tMagdeli,   @tLindsay,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA5, @tSameer,    @tChristan,  '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA5, @tTimothy,   @tJarryd,    '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA6, @tMagdeli,   @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA6, @tSameer,    @tJaunte,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA6, @tTimothy,   @tLindsay,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA6, @tJarryd,    @tChristan,  '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA7, @tSameer,    @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA7, @tTimothy,   @tMagdeli,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA7, @tJarryd,    @tJaunte,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA7, @tChristan,  @tLindsay,   '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA8, @tTimothy,   @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA8, @tJarryd,    @tSameer,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA8, @tChristan,  @tMagdeli,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA8, @tLindsay,   @tJaunte,    '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA9, @tJarryd,    @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA9, @tChristan,  @tTimothy,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA9, @tLindsay,   @tSameer,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA9, @tJaunte,    @tMagdeli,   '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA10, @tSulaimaan, @tChristan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA10, @tJarryd,    @tLindsay,  '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA10, @tTimothy,   @tJaunte,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA10, @tSameer,    @tMagdeli,  '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA11, @tChristan,  @tLindsay,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA11, @tJaunte,    @tMagdeli,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA11, @tSameer,    @tTimothy,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA11, @tJarryd,    @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING');

/* Sunrise league fixtures (round 12). */
INSERT INTO `fixture`
    (fixtureId, leagueId, roundId, team_a_id, team_b_id, fixtureDate, fixtureTime, status)
VALUES
    (UUID(), @lgSunrise, @rB, @tChristan, @tSulaimaan, '2026-07-24', '09:00:00', 'UPCOMING'),
    (UUID(), @lgSunrise, @rB, @tLindsay,  @tJaunte,    '2026-07-24', '09:00:00', 'UPCOMING'),
    (UUID(), @lgSunrise, @rB, @tMagdeli,  @tSameer,    '2026-07-24', '09:00:00', 'UPCOMING'),
    (UUID(), @lgSunrise, @rB, @tTimothy,  @tJarryd,    '2026-07-24', '09:00:00', 'UPCOMING');

/* Test league fixtures (round 13). */
INSERT INTO `fixture`
    (fixtureId, leagueId, roundId, team_a_id, team_b_id, fixtureDate, fixtureTime, status)
VALUES
    (UUID(), @lgTest, @rT, @tChristan, @tMagdeli,   '2026-07-23', '20:00:00', 'UPCOMING'),
    (UUID(), @lgTest, @rT, @tLindsay,  @tTimothy,   '2026-07-23', '20:00:00', 'UPCOMING'),
    (UUID(), @lgTest, @rT, @tJaunte,   @tJarryd,    '2026-07-23', '20:00:00', 'UPCOMING'),
    (UUID(), @lgTest, @rT, @tSameer,   @tSulaimaan, '2026-07-23', '20:00:00', 'UPCOMING');

/* LEAGUE-scope leaderboards + zeroed rankings so the new leagues show standings. */
SET @lbShowcase = UUID();
SET @lbSunrise  = UUID();
SET @lbTest     = UUID();

INSERT INTO `leaderboard` (leaderboardId, leagueId, season, scope)
VALUES
    (@lbShowcase, @lgShowcase, '2026', 'LEAGUE'),
    (@lbSunrise,  @lgSunrise,  '2026', 'LEAGUE'),
    (@lbTest,     @lgTest,     '2026', 'LEAGUE');

INSERT INTO `ranking`
    (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
     matchesPlayed, matchesWon, matchesDrawn, matchesLost,
     pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
SELECT UUID(), r.leaderboardId, r.teamId, r.rn, NULL,
       0, 0, 0, 0, 0, 0, 0, 0
FROM (
    SELECT lb.leaderboardId,
           m.teamId,
           ROW_NUMBER() OVER (PARTITION BY lb.leaderboardId ORDER BY m.joinDate, m.teamId) AS rn
    FROM `leaderboard` lb
    JOIN `leagueMembership` m ON m.leagueId = lb.leagueId AND m.isActive = TRUE
    WHERE lb.leaderboardId IN (@lbShowcase, @lbSunrise, @lbTest)
) AS r;

/* Register the 11 new teams on the MASTER leaderboard as well, ranked after
   the teams the base seed already placed there. */
SET @masterLb  = (SELECT leaderboardId FROM `leaderboard` WHERE scope = 'MASTER' AND season = '2026');
SET @masterMax = (SELECT COALESCE(MAX(currentRanking), 0) FROM `ranking` WHERE leaderboardId = @masterLb);

INSERT INTO `ranking`
    (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
     matchesPlayed, matchesWon, matchesDrawn, matchesLost,
     pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
SELECT UUID(), @masterLb, t.teamId,
       @masterMax + ROW_NUMBER() OVER (ORDER BY t.teamName), NULL,
       0, 0, 0, 0, 0, 0, 0, 0
FROM `fantasyTeam` t
WHERE t.teamId IN (@tChristan, @tLindsay, @tJaunte, @tMagdeli, @tSameer,
                   @tTimothy, @tJarryd, @tSulaimaan, @tUser1, @tUser2, @tUser3)
  AND @masterLb IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `ranking` rk
                  WHERE rk.leaderboardId = @masterLb AND rk.teamId = t.teamId);

/* Renumber the MASTER-scope leaderboard's positions so they are contiguous 1..N.
   The base-seed rows above (positions 1, 2, 3, 4, 16) and the just-registered 11
   teams (positions @masterMax+1 .. @masterMax+11) were assigned independently, so
   the combined result has a 5-15 gap and Sarah Sharks (leaguePoints 0, scoreDifference
   -1) sits above the zero-point unplayed teams instead of below them. This block
   fixes only the final currentRanking numbers -- no stat values are touched -- and
   only for the season-2026 MASTER board; LEAGUE-scope boards and the season-2025
   archive block are untouched.

   `uk_ranking_position` is UNIQUE on (leaderboardId, currentRanking), so a single
   UPDATE that reassigns 1..N directly can collide mid-statement with a row that
   still holds one of the target positions. This mirrors the two-pass renumber in
   LeaderboardServiceImpl.refreshRankings: first shift every MASTER row to a
   temporary, guaranteed non-colliding range (+1000), then assign the final 1..N.
   The final ordering must match LeaderboardAggregator.rankingOrder(): leaguePoints
   DESC, then scoreDifference DESC (the stored generated pointsFor - pointsAgainst
   column, read here rather than recomputed), then total_fantasy_points DESC. */
UPDATE `ranking`
SET currentRanking = currentRanking + 1000
WHERE leaderboardId = @masterLb;

SET @pos := 0;

UPDATE `ranking` r
JOIN (
    SELECT rankingId,
           (@pos := @pos + 1) AS newPos
    FROM `ranking`
    WHERE leaderboardId = @masterLb
    ORDER BY leaguePoints DESC, scoreDifference DESC, total_fantasy_points DESC
) AS ordered ON ordered.rankingId = r.rankingId
SET r.currentRanking = ordered.newPos;

COMMIT;


/* ============================================================================
   SECTION 5 -- seed-extra-leagues.sql (Highveld Heroes, Coastal Cup, Varsity
   Challenge, Office Rugby Pool, Old Boys XV). Adapted: @meUsername -> 'christan'
   (created in Section 3). Runs verbatim otherwise, including its own guards,
   transaction and before/after report queries.
   ============================================================================ */
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
SET @meUsername = 'christan';

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
WHERE l.leagueName = 'Office Rugby Pool'   -- PRIVATE; Highveld Heroes is public and has no manager
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
--    positions come from ROW_NUMBER(); previousRanking is left as rn + 1 to
--    give the rank movement chips something to show.
--    Stat columns are DERIVED from a live LeaderboardServiceImpl /
--    LeaderboardAggregator recompute against this exact seed: these five
--    leagues (Coastal Cup, Highveld Heroes, Office Rugby Pool, Old Boys XV,
--    Varsity Challenge) have zero fixtures and zero matchResults behind them,
--    so the recompute correctly zeroes every row rather than leaving the
--    hand-authored numbers below. Regenerate rather than hand-edit if
--    fixtures/results for these leagues are ever added.
-- ---------------------------------------------------------------------------
INSERT INTO `ranking` (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
                       matchesPlayed, matchesWon, matchesDrawn, matchesLost,
                       pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
SELECT UUID(), r.leaderboardId, r.teamId, r.rn, r.rn + 1,
       0, 0, 0, 0,
       0, 0, 0, 0
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


/* ============================================================================
   SECTION 6 -- Past-season simulation settings and scoring rules
   ----------------------------------------------------------------------------
   Historical, INACTIVE simulation-settings versions for prior seasons so the
   admin simulation-settings screen (SimulationSettingServiceImpl.listSimulation-
   Settings returns every season) can show and manage past seasons alongside the
   active 2026 configuration. Only the 2026 row stays active -- the generated
   uk_simulationSettings_active_season key allows exactly one active season, so
   these must be isActive = FALSE (and therefore allowResimulation = FALSE with
   maxResimulations = 0, per chk_simulationSettings_resimulation_enabled).
   Matching scoring-rule sets are added for each past season; they have no
   results yet, so they stay editable.
   ============================================================================ */
START TRANSACTION;

SET @simSettings2024 = UUID();
SET @simSettings2025 = UUID();

INSERT INTO `simulationSettings`
    (settingsId, season, settingsVersion,
     player_ability_weight, player_form_weight, team_balance_weight, random_variation_weight,
     require_admin_approval, allowResimulation, maxResimulations, isActive)
VALUES
    (@simSettings2024, '2024', 1, 40.00, 25.00, 20.00, 15.00, TRUE, FALSE, 0, FALSE),
    (@simSettings2025, '2025', 1, 35.00, 30.00, 20.00, 15.00, TRUE, FALSE, 0, FALSE);

INSERT INTO `scoringRule`
    (ruleId, season, eventType, pointsAwarded, isDeduction, description)
VALUES
    (UUID(), '2024', 'TRY',           5, FALSE, 'Points awarded for scoring a try'),
    (UUID(), '2024', 'ASSIST',        3, FALSE, 'Points awarded for a try assist'),
    (UUID(), '2024', 'TACKLE',        1, FALSE, 'Points awarded for a successful tackle'),
    (UUID(), '2024', 'CONVERSION',    2, FALSE, 'Points awarded for a successful conversion'),
    (UUID(), '2024', 'MISSED_TACKLE', 1, TRUE,  'Deduction for a missed tackle'),
    (UUID(), '2024', 'YELLOW_CARD',   3, TRUE,  'Deduction for a yellow card'),
    (UUID(), '2024', 'RED_CARD',     10, TRUE,  'Deduction for a red card'),

    (UUID(), '2025', 'TRY',           5, FALSE, 'Points awarded for scoring a try'),
    (UUID(), '2025', 'ASSIST',        3, FALSE, 'Points awarded for a try assist'),
    (UUID(), '2025', 'TACKLE',        1, FALSE, 'Points awarded for a successful tackle'),
    (UUID(), '2025', 'CONVERSION',    2, FALSE, 'Points awarded for a successful conversion'),
    (UUID(), '2025', 'MISSED_TACKLE', 1, TRUE,  'Deduction for a missed tackle'),
    (UUID(), '2025', 'YELLOW_CARD',   3, TRUE,  'Deduction for a yellow card'),
    (UUID(), '2025', 'RED_CARD',     10, TRUE,  'Deduction for a red card');

COMMIT;


/* ============================================================================
   SECTION 7 -- Closed (finished) leagues
   ----------------------------------------------------------------------------
   Two leagues with isActive = FALSE, each carrying final standings, so the
   presentation can show the "closed league" state next to the live ones.
   Closed leagues remain visible in the full leagues list
   (LeagueServiceImpl.getAllLeagues does not filter on isActive) but are hidden
   from the joinable public preview (getPublicLeaguePreviews skips
   isActive = FALSE), so watchers cannot accidentally join a finished league.
   Members reuse the base demo teams (john/sarah/mike/emma/david), re-resolved
   by username so the section is independent of Section 1's session variables.
   ============================================================================ */
START TRANSACTION;

SET @cuJohn  = (SELECT userId FROM `user` WHERE username = 'john');
SET @cuSarah = (SELECT userId FROM `user` WHERE username = 'sarah');
SET @cuMike  = (SELECT userId FROM `user` WHERE username = 'mike');
SET @cuEmma  = (SELECT userId FROM `user` WHERE username = 'emma');
SET @cuDavid = (SELECT userId FROM `user` WHERE username = 'david');

SET @ctJohn  = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuJohn);
SET @ctSarah = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuSarah);
SET @ctMike  = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuMike);
SET @ctEmma  = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuEmma);
SET @ctDavid = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuDavid);

SET @lgAutumn  = UUID();
SET @lgLegends = UUID();

/* Created active with a NULL manager (trg_league_manager_insert forbids setting
   a manager on INSERT); the manager is assigned after memberships exist and the
   leagues are closed at the very end of the section. */
INSERT INTO `league`
    (leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, maxMembers)
VALUES
    (@lgAutumn, NULL, 'Autumn Classic 2025',
     'Completed 2025 public league. Final standings only - this league is closed.',
     'PUBLIC', NULL, 20),
    (@lgLegends, NULL, 'Legends Invitational 2025',
     'Completed 2025 invitational. Closed - kept for its final leaderboard.',
     'PRIVATE', 'LEG789', 12);

INSERT INTO `leagueMembership` (membershipId, leagueId, registered_user_id, teamId)
VALUES
    (UUID(), @lgAutumn, @cuJohn,  @ctJohn),
    (UUID(), @lgAutumn, @cuSarah, @ctSarah),
    (UUID(), @lgAutumn, @cuMike,  @ctMike),
    (UUID(), @lgAutumn, @cuEmma,  @ctEmma),
    (UUID(), @lgAutumn, @cuDavid, @ctDavid),

    (UUID(), @lgLegends, @cuSarah, @ctSarah),
    (UUID(), @lgLegends, @cuMike,  @ctMike),
    (UUID(), @lgLegends, @cuEmma,  @ctEmma);

/* Autumn Classic 2025 is PUBLIC and therefore has no manager. */
UPDATE `league` SET manager_user_id = @cuSarah WHERE leagueId = @lgLegends;

SET @lbAutumn  = UUID();
SET @lbLegends = UUID();

INSERT INTO `leaderboard` (leaderboardId, leagueId, season, scope)
VALUES
    (@lbAutumn,  @lgAutumn,  '2025', 'LEAGUE'),
    (@lbLegends, @lgLegends, '2025', 'LEAGUE');

INSERT INTO `ranking`
    (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
     matchesPlayed, matchesWon, matchesDrawn, matchesLost,
     pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
VALUES
    (UUID(), @lbAutumn, @ctJohn,  1, 2, 4, 3, 0, 1, 82, 60, 12, 82),
    (UUID(), @lbAutumn, @ctSarah, 2, 1, 4, 2, 1, 1, 74, 66,  8, 74),
    (UUID(), @lbAutumn, @ctMike,  3, 3, 4, 2, 0, 2, 68, 70,  8, 68),
    (UUID(), @lbAutumn, @ctEmma,  4, 4, 4, 1, 1, 2, 61, 72,  5, 61),
    (UUID(), @lbAutumn, @ctDavid, 5, 5, 4, 0, 2, 2, 55, 72,  2, 55),

    (UUID(), @lbLegends, @ctSarah, 1, 1, 3, 3, 0, 0, 60, 40,  9, 60),
    (UUID(), @lbLegends, @ctMike,  2, 3, 3, 1, 1, 1, 48, 47,  5, 48),
    (UUID(), @lbLegends, @ctEmma,  3, 2, 3, 0, 1, 2, 38, 59,  1, 38);

/* Close both leagues now that their memberships, manager and standings exist. */
UPDATE `league` SET isActive = FALSE WHERE leagueId IN (@lgAutumn, @lgLegends);

COMMIT;

/* Presentation cheat-sheet: which leagues are live vs closed, and joinable room. */
SELECT l.leagueName,
       l.leagueType,
       CASE WHEN l.isActive THEN 'LIVE' ELSE 'CLOSED' END                             AS state,
       l.maxMembers,
       (SELECT COUNT(*) FROM `leagueMembership` m
         WHERE m.leagueId = l.leagueId AND m.isActive = TRUE)                          AS members
FROM `league` l
ORDER BY l.isActive DESC, l.leagueType, l.leagueName;


/* ============================================================================
   SECTION 8 -- Real URC squads imported from the live player feed
   ----------------------------------------------------------------------------
   The admin "Refresh from live feed" page (PlayerFeedClient -> nexulartechnologies
   endpoint) is no longer reachable, but a prior successful refresh's output was
   saved to JSON and is baked in here so a fresh reseed still gets the real 2024/25
   URC squads instead of just the 33 hand-written sample players from Section 1.
   Feed clubId/positionId -> name resolved via the exact same lookup the real
   import uses (ExternalCatalogMapping), then re-resolved here to Section 1's
   @xxxClub/@xxxId session variables. Cheetahs and Pumas get no rows because the
   feed only covers the 16 URC teams (see ExternalCatalogMapping's javadoc).
   897 players inserted, isActive = TRUE, no playerAvailability rows --
   matching PlayerImportServiceImpl.applyFeedImport, which does not touch that table.
   ============================================================================ */
START TRANSACTION;

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (UUID(), @lionsClub, @centreId, 'Henco van Wyk', 10.90, 67, 55, 41, 62, 81, 92, 78, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Francke Horn', 9.90, 60, 49, 39, 77, 87, 93, 71, 1),
    (UUID(), @connachtClub, @looseForwardId, 'Sean Jansen', 9.90, 67, 62, 23, 64, 75, 86, 76, 1),
    (UUID(), @dragonsClub, @looseForwardId, 'Aaron Wainwright', 9.80, 62, 54, 40, 71, 77, 94, 72, 1),
    (UUID(), @munsterClub, @centreId, 'Alex Nankivell', 9.60, 59, 62, 25, 58, 84, 92, 72, 1),
    (UUID(), @stormersClub, @flyhalfId, 'Sacha Feinberg-Mngomezulu', 9.60, 77, 44, 69, 37, 73, 95, 75, 1),
    (UUID(), @dragonsClub, @centreId, 'Aneurin Owen', 9.50, 40, 62, 46, 82, 85, 96, 69, 1),
    (UUID(), @ospreysClub, @flyhalfId, 'Jack Walsh', 9.50, 66, 46, 40, 71, 78, 91, 75, 1),
    (UUID(), @leinsterClub, @centreId, 'Jimmy O''Brien', 9.30, 75, 30, 83, 84, 78, 93, 60, 1),
    (UUID(), @lionsClub, @fullbackId, 'Quan Horn', 9.30, 61, 36, 84, 57, 87, 97, 70, 1),
    (UUID(), @connachtClub, @looseForwardId, 'Paul Boyle', 9.20, 63, 62, 28, 64, 73, 77, 79, 1),
    (UUID(), @glasgowWarriorsClub, @centreId, 'Stafford McDowall', 9.20, 65, 38, 71, 69, 82, 94, 68, 1),
    (UUID(), @ospreysClub, @lockId, 'James Ratti', 9.10, 65, 51, 14, 88, 84, 91, 62, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Fletcher Anderson', 8.70, 61, 56, 6, 64, 78, 94, 73, 1),
    (UUID(), @leinsterClub, @lockId, 'Joe McCarthy', 8.70, 74, 69, 15, 56, 63, 74, 74, 1),
    (UUID(), @ulsterClub, @hookerId, 'Tom Stewart', 8.70, 65, 68, 15, 63, 69, 77, 75, 1),
    (UUID(), @edinburghClub, @hookerId, 'Ewan Ashman', 8.60, 70, 58, 27, 56, 71, 83, 72, 1),
    (UUID(), @ulsterClub, @wingId, 'Zac Ward', 8.60, 72, 49, 45, 49, 77, 94, 64, 1),
    (UUID(), @bullsClub, @lockId, 'Ruan Nortje', 8.50, 66, 61, 6, 29, 75, 93, 80, 1),
    (UUID(), @glasgowWarriorsClub, @centreId, 'Sione Tuipulotu', 8.50, 57, 45, 54, 83, 81, 89, 66, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'Nick Timoney', 8.40, 46, 64, 20, 89, 79, 92, 71, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Ruan Venter', 8.20, 76, 46, 20, 52, 77, 83, 74, 1),
    (UUID(), @glasgowWarriorsClub, @flyhalfId, 'Dan Lancaster', 8.10, 62, 62, 50, 68, 66, 70, 72, 1),
    (UUID(), @lionsClub, @propId, 'Asenathi Ntlabakanye', 7.90, 70, 60, 16, 55, 69, 75, 76, 1),
    (UUID(), @cardiffClub, @fullbackId, 'Cam Winnett', 7.90, 58, 38, 73, 85, 83, 93, 55, 1),
    (UUID(), @bullsClub, @scrumhalfId, 'Embrose Papier', 7.90, 69, 47, 53, 65, 75, 80, 64, 1),
    (UUID(), @lionsClub, @centreId, 'Erich Cronje', 7.90, 65, 64, 61, 48, 59, 73, 73, 1),
    (UUID(), @bullsClub, @hookerId, 'Johann Grobbelaar', 7.90, 54, 60, 10, 69, 74, 83, 77, 1),
    (UUID(), @dragonsClub, @flyhalfId, 'Tinus de Beer', 7.90, 51, 61, 50, 69, 80, 88, 63, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Evan Roos', 7.80, 64, 55, 27, 42, 78, 93, 67, 1),
    (UUID(), @bullsClub, @wingId, 'Sebastian de Klerk', 7.80, 69, 46, 49, 73, 73, 86, 59, 1),
    (UUID(), @glasgowWarriorsClub, @wingId, 'Ollie Smith', 7.70, 53, 50, 64, 73, 76, 87, 61, 1),
    (UUID(), @zebreParmaClub, @centreId, 'Giulio Bertaccini', 7.60, 44, 66, 12, 55, 84, 90, 70, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'Juarno Augustus', 7.60, 69, 62, 10, 77, 63, 73, 69, 1),
    (UUID(), @benettonClub, @fullbackId, 'Matt Gallagher', 7.60, 57, 37, 68, 94, 77, 86, 61, 1),
    (UUID(), @edinburghClub, @wingId, 'Darcy Graham', 7.50, 54, 41, 80, 69, 73, 92, 66, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Sibabalwe Mahashe', 7.50, 63, 54, 49, 73, 63, 83, 65, 1),
    (UUID(), @edinburghClub, @wingId, 'Wes Goosen', 7.50, 55, 44, 55, 70, 78, 90, 70, 1),
    (UUID(), @glasgowWarriorsClub, @scrumhalfId, 'George Horne', 7.40, 68, 49, 62, 76, 68, 79, 69, 1),
    (UUID(), @ospreysClub, @centreId, 'Keiran Williams', 7.40, 64, 55, 14, 57, 70, 85, 72, 1),
    (UUID(), @glasgowWarriorsClub, @wingId, 'Kyle Rowe', 7.40, 61, 42, 61, 69, 77, 93, 58, 1),
    (UUID(), @lionsClub, @scrumhalfId, 'Morne van den Berg', 7.40, 60, 60, 58, 64, 62, 76, 68, 1),
    (UUID(), @ulsterClub, @scrumhalfId, 'Nathan Doak', 7.30, 48, 52, 61, 60, 78, 86, 69, 1),
    (UUID(), @leinsterClub, @flyhalfId, 'Sam Prendergast', 7.30, 67, 40, 72, 75, 65, 82, 64, 1),
    (UUID(), @stormersClub, @centreId, 'Damian Willemse', 7.20, 52, 47, 70, 50, 78, 96, 62, 1),
    (UUID(), @scarletsClub, @centreId, 'Joe Roberts', 7.20, 61, 53, 57, 30, 76, 89, 66, 1),
    (UUID(), @zebreParmaClub, @fullbackId, 'Mirko Belloni', 7.20, 60, 56, 26, 70, 70, 82, 67, 1),
    (UUID(), @connachtClub, @centreId, 'Cathal Forde', 7.10, 45, 60, 43, 65, 75, 85, 66, 1),
    (UUID(), @benettonClub, @centreId, 'Tommaso Menoncello', 7.10, 63, 56, 60, 44, 66, 86, 62, 1),
    (UUID(), @munsterClub, @centreId, 'Tom Farrell', 7.00, 65, 58, 72, 54, 69, 80, 69, 1),
    (UUID(), @leinsterClub, @lockId, 'Alex Soroka', 6.90, 64, 64, 23, 75, 63, 70, 62, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'Dave McCann', 6.90, 56, 54, 19, 51, 81, 90, 64, 1),
    (UUID(), @ospreysClub, @looseForwardId, 'Harri Deaves', 6.90, 62, 60, 29, 53, 67, 75, 70, 1),
    (UUID(), @dragonsClub, @lockId, 'Ben Carter', 6.80, 29, 70, 19, 52, 86, 96, 66, 1),
    (UUID(), @munsterClub, @centreId, 'Dan Kelly', 6.80, 45, 47, 66, 79, 74, 83, 67, 1),
    (UUID(), @bullsClub, @fullbackId, 'David Kriel', 6.80, 51, 56, 31, 56, 77, 85, 67, 1),
    (UUID(), @dragonsClub, @centreId, 'Fine Inisi', 6.80, 46, 51, 41, 59, 87, 89, 63, 1),
    (UUID(), @ospreysClub, @fullbackId, 'Iestyn Hopkins', 6.80, 59, 54, 41, 87, 64, 76, 62, 1),
    (UUID(), @leinsterClub, @centreId, 'Jamie Osborne', 6.80, 62, 50, 70, 67, 68, 84, 52, 1),
    (UUID(), @lionsClub, @propId, 'SJ Kotze', 6.80, 75, 48, 18, 35, 78, 80, 65, 1),
    (UUID(), @connachtClub, @flyhalfId, 'Josh Ioane', 6.70, 71, 44, 31, 83, 71, 80, 66, 1),
    (UUID(), @glasgowWarriorsClub, @scrumhalfId, 'Jamie Dobie', 6.60, 72, 51, 43, 65, 66, 78, 54, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Magnus Bradbury', 6.60, 54, 56, 6, 54, 77, 90, 74, 1),
    (UUID(), @sharksClub, @centreId, 'Andre Esterhuizen', 6.50, 66, 34, 69, 72, 80, 92, 63, 1),
    (UUID(), @bullsClub, @centreId, 'Harold Vorster', 6.50, 60, 49, 39, 75, 80, 84, 66, 1),
    (UUID(), @cardiffClub, @centreId, 'Ben Thomas', 6.40, 55, 52, 64, 19, 78, 95, 60, 1),
    (UUID(), @lionsClub, @centreId, 'Bronson Mills', 6.40, 57, 54, 71, 42, 68, 81, 62, 1),
    (UUID(), @ulsterClub, @lockId, 'Cormac Izuchukwu', 6.30, 73, 59, 13, 71, 57, 72, 59, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Gavin Coombes', 6.30, 54, 68, 13, 45, 72, 78, 66, 1),
    (UUID(), @ulsterClub, @wingId, 'Jacob Stockdale', 6.30, 65, 40, 72, 63, 67, 84, 61, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Paul de Villiers', 6.30, 46, 63, 5, 73, 75, 81, 65, 1),
    (UUID(), @connachtClub, @looseForwardId, 'Cian Prendergast', 6.20, 51, 61, 6, 43, 76, 90, 66, 1),
    (UUID(), @ospreysClub, @flyhalfId, 'Dan Edwards', 6.20, 49, 31, 65, 85, 78, 94, 57, 1),
    (UUID(), @ospreysClub, @lockId, 'Ryan Smith', 6.20, 66, 59, 14, 27, 70, 83, 67, 1),
    (UUID(), @zebreParmaClub, @wingId, 'Simone Gesi', 6.20, 47, 45, 42, 53, 81, 94, 65, 1),
    (UUID(), @bullsClub, @wingId, 'Canan Moodie', 6.10, 62, 45, 75, 27, 65, 84, 69, 1),
    (UUID(), @zebreParmaClub, @flyhalfId, 'Giovanni Montemauri', 6.10, 63, 47, 45, 92, 61, 72, 59, 1),
    (UUID(), @glasgowWarriorsClub, @looseForwardId, 'Gregor Brown', 6.10, 63, 63, 24, 66, 60, 65, 66, 1),
    (UUID(), @benettonClub, @wingId, 'Louis Lynagh', 6.00, 61, 50, 53, 55, 63, 87, 57, 1),
    (UUID(), @glasgowWarriorsClub, @looseForwardId, 'Matt Fagerson', 6.00, 53, 67, 26, 45, 70, 82, 58, 1),
    (UUID(), @benettonClub, @wingId, 'Paolo Odogwu', 6.00, 46, 58, 9, 49, 76, 89, 69, 1),
    (UUID(), @connachtClub, @looseForwardId, 'Shamus Hurley-Langton', 6.00, 69, 65, 24, 50, 57, 70, 62, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Cameron Hanekom', 5.90, 69, 60, 25, 63, 60, 71, 57, 1),
    (UUID(), @glasgowWarriorsClub, @fullbackId, 'Josh McKay', 5.90, 67, 25, 44, 49, 85, 96, 58, 1),
    (UUID(), @glasgowWarriorsClub, @wingId, 'Kyle Steyn', 5.90, 63, 44, 58, 53, 74, 89, 68, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Phepsi Buthelezi', 5.90, 52, 52, 28, 64, 72, 77, 69, 1),
    (UUID(), @ospreysClub, @lockId, 'Rhys Davies', 5.90, 39, 67, 25, 57, 74, 81, 66, 1),
    (UUID(), @dragonsClub, @wingId, 'Rio Dyer', 5.90, 58, 35, 52, 63, 80, 91, 56, 1),
    (UUID(), @bullsClub, @lockId, 'Ruan Vermaak', 5.90, 63, 56, 18, 66, 61, 68, 71, 1),
    (UUID(), @cardiffClub, @flyhalfId, 'Callum Sheedy', 5.80, 39, 46, 60, 85, 82, 90, 53, 1),
    (UUID(), @leinsterClub, @flyhalfId, 'Ciaran Frawley', 5.80, 58, 50, 35, 68, 71, 76, 61, 1),
    (UUID(), @edinburghClub, @centreId, 'James Lang', 5.80, 54, 49, 68, 65, 73, 86, 57, 1),
    (UUID(), @scarletsClub, @lockId, 'Max Douglas', 5.80, 57, 58, 5, 12, 83, 93, 64, 1),
    (UUID(), @lionsClub, @wingId, 'Richard Kriel', 5.80, 44, 56, 48, 69, 68, 80, 64, 1),
    (UUID(), @munsterClub, @centreId, 'Shane Daly', 5.80, 48, 28, 77, 69, 77, 92, 64, 1),
    (UUID(), @munsterClub, @lockId, 'Fineen Wycherley', 5.70, 31, 61, 22, 70, 79, 86, 65, 1),
    (UUID(), @cardiffClub, @fullbackId, 'Jacob Beetham', 5.70, 49, 62, 34, 43, 70, 85, 60, 1),
    (UUID(), @benettonClub, @flyhalfId, 'Jacob Umaga', 5.70, 52, 38, 56, 69, 75, 94, 54, 1),
    (UUID(), @ulsterClub, @centreId, 'Jude Postlethwaite', 5.70, 64, 60, 14, 76, 57, 68, 63, 1),
    (UUID(), @bullsClub, @wingId, 'Kurt-Lee Arendse', 5.70, 60, 41, 44, 39, 76, 89, 68, 1),
    (UUID(), @leinsterClub, @wingId, 'Rieko Ioane', 5.70, 50, 47, 36, 53, 74, 91, 63, 1),
    (UUID(), @scarletsClub, @wingId, 'Tom Rogers', 5.70, 43, 49, 67, 57, 73, 87, 61, 1),
    (UUID(), @cardiffClub, @centreId, 'Harri Millard', 5.60, 38, 48, 51, 82, 79, 91, 51, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'Bryn Ward', 5.50, 71, 56, 10, 90, 52, 63, 59, 1),
    (UUID(), @ospreysClub, @lockId, 'Huw Sutton', 5.50, 52, 65, 17, 60, 66, 68, 67, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'James Culhane', 5.50, 66, 56, 21, 72, 56, 64, 64, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Marcell Coetzee', 5.50, 60, 60, 27, 83, 79, 78, 75, 1),
    (UUID(), @ospreysClub, @centreId, 'Owen Watkin', 5.50, 37, 59, 61, 71, 70, 83, 55, 1),
    (UUID(), @glasgowWarriorsClub, @propId, 'Patrick Schickerling', 5.50, 69, 63, 21, 51, 56, 62, 66, 1),
    (UUID(), @stormersClub, @fullbackId, 'Warrick Gelant', 5.50, 48, 35, 80, 82, 76, 84, 63, 1),
    (UUID(), @munsterClub, @flyhalfId, 'Jack Crowley', 5.40, 53, 61, 68, 49, 60, 80, 51, 1),
    (UUID(), @sharksClub, @centreId, 'Jurenzo Julius', 5.40, 62, 42, 49, 77, 59, 73, 63, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'Taine Basham', 5.40, 66, 56, 26, 41, 63, 72, 64, 1),
    (UUID(), @munsterClub, @lockId, 'Tom Ahern', 5.40, 66, 43, 8, 84, 62, 75, 63, 1),
    (UUID(), @sharksClub, @wingId, 'Edwill van der Merwe', 5.30, 45, 33, 77, 64, 79, 94, 60, 1),
    (UUID(), @connachtClub, @fullbackId, 'Shane Jennings', 5.30, 30, 66, 18, 80, 73, 92, 53, 1),
    (UUID(), @leinsterClub, @wingId, 'Tommy O''Brien', 5.30, 65, 51, 47, 36, 63, 79, 59, 1),
    (UUID(), @scarletsClub, @wingId, 'Blair Murray', 5.20, 50, 28, 86, 59, 79, 93, 52, 1),
    (UUID(), @benettonClub, @lockId, 'Riccardo Favretto', 5.20, 54, 43, 18, 70, 76, 81, 62, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Taine Plumtree', 5.20, 62, 51, 35, 38, 68, 86, 55, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Vincent Tshituka', 5.20, 54, 39, 21, 39, 81, 96, 64, 1),
    (UUID(), @ulsterClub, @lockId, 'Charlie Irvine', 5.10, 44, 67, 26, 50, 69, 82, 55, 1),
    (UUID(), @connachtClub, @centreId, 'Finn Treacy', 5.10, 63, 44, 67, 90, 49, 63, 57, 1),
    (UUID(), @zebreParmaClub, @fullbackId, 'Giacomo da Re', 5.10, 33, 51, 63, 94, 66, 87, 52, 1),
    (UUID(), @ulsterClub, @centreId, 'James Hume', 5.10, 45, 44, 69, 54, 76, 90, 53, 1),
    (UUID(), @cardiffClub, @scrumhalfId, 'Johan Mulder', 5.10, 48, 63, 61, 41, 63, 72, 61, 1),
    (UUID(), @connachtClub, @lockId, 'Josh Murphy', 5.10, 51, 62, 25, 32, 75, 78, 70, 1),
    (UUID(), @ospreysClub, @wingId, 'Keelan Giles', 5.10, 54, 47, 48, 34, 74, 87, 59, 1),
    (UUID(), @connachtClub, @fullbackId, 'Shayne Bolton', 5.10, 60, 51, 21, 68, 59, 74, 63, 1),
    (UUID(), @sharksClub, @lockId, 'Emile van Heerden', 5.00, 58, 51, 8, 58, 69, 81, 61, 1),
    (UUID(), @edinburghClub, @flyhalfId, 'Ross Thompson', 5.00, 43, 57, 43, 57, 65, 77, 66, 1),
    (UUID(), @benettonClub, @propId, 'Thomas Gallo', 5.00, 70, 67, 21, 40, 49, 61, 66, 1),
    (UUID(), @ulsterClub, @centreId, 'Werner Kok', 5.00, 65, 52, 31, 32, 78, 86, 69, 1),
    (UUID(), @stormersClub, @lockId, 'Ben-Jason Dixon', 4.90, 52, 52, 5, 33, 72, 82, 73, 1),
    (UUID(), @lionsClub, @flyhalfId, 'Chris Smith', 4.90, 42, 40, 64, 66, 83, 91, 56, 1),
    (UUID(), @cardiffClub, @flyhalfId, 'Ioan Lloyd', 4.90, 64, 43, 44, 77, 57, 62, 65, 1),
    (UUID(), @scarletsClub, @centreId, 'Johnny Williams', 4.90, 47, 53, 42, 54, 72, 82, 56, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Manu Tshituka', 4.90, 58, 49, 9, 62, 67, 76, 63, 1),
    (UUID(), @cardiffClub, @wingId, 'Tom Bowen', 4.90, 48, 40, 70, 67, 68, 83, 53, 1),
    (UUID(), @stormersClub, @hookerId, 'Andre-Hugo Venter', 4.80, 53, 52, 28, 57, 70, 74, 59, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Brian Gleeson', 4.80, 60, 63, 18, 57, 61, 64, 57, 1),
    (UUID(), @edinburghClub, @lockId, 'Glen Young', 4.80, 70, 57, 7, 35, 62, 71, 73, 1),
    (UUID(), @munsterClub, @fullbackId, 'Mike Haley', 4.80, 56, 41, 54, 68, 69, 81, 67, 1),
    (UUID(), @ulsterClub, @wingId, 'Robert Baloucoune', 4.80, 64, 48, 34, 76, 56, 70, 57, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Scott Penny', 4.80, 58, 57, 16, 50, 59, 71, 69, 1),
    (UUID(), @stormersClub, @centreId, 'Wandisile Simelane', 4.80, 49, 29, 57, 79, 78, 84, 55, 1),
    (UUID(), @ulsterClub, @propId, 'Angus Bell', 4.70, 71, 53, 18, 66, 59, 61, 55, 1),
    (UUID(), @leinsterClub, @flyhalfId, 'Charlie Tector', 4.70, 68, 57, 49, 36, 55, 67, 58, 1),
    (UUID(), @benettonClub, @wingId, 'Ignacio Mendy', 4.70, 64, 27, 15, 60, 75, 93, 59, 1),
    (UUID(), @lionsClub, @centreId, 'Kelly Mpeku', 4.70, 63, 44, 66, 81, 52, 58, 61, 1);

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (UUID(), @stormersClub, @propId, 'Ntuthuko Mchunu', 4.70, 76, 40, 26, 40, 63, 70, 63, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'Alun Lawrence', 4.60, 39, 58, 6, 61, 73, 81, 65, 1),
    (UUID(), @dragonsClub, @fullbackId, 'Angus O''Brien', 4.60, 43, 25, 75, 86, 80, 92, 58, 1),
    (UUID(), @zebreParmaClub, @looseForwardId, 'Bautista Stavile', 4.60, 66, 53, 30, 52, 58, 71, 56, 1),
    (UUID(), @lionsClub, @centreId, 'Eduan Keyter', 4.60, 66, 48, 47, 76, 54, 74, 50, 1),
    (UUID(), @bullsClub, @flyhalfId, 'Handre Pollard', 4.60, 50, 36, 61, 70, 76, 86, 64, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Jarrod Taylor', 4.60, 54, 58, 7, 41, 65, 74, 69, 1),
    (UUID(), @cardiffClub, @propId, 'Danny Southworth', 4.50, 61, 59, 24, 77, 49, 57, 63, 1),
    (UUID(), @scarletsClub, @centreId, 'Macs Page', 4.50, 58, 41, 66, 55, 63, 73, 55, 1),
    (UUID(), @edinburghClub, @centreId, 'Matt Currie', 4.50, 48, 54, 53, 77, 56, 77, 51, 1),
    (UUID(), @stormersClub, @centreId, 'Ruhan Nel', 4.50, 53, 44, 59, 59, 83, 89, 71, 1),
    (UUID(), @scarletsClub, @centreId, 'Eddie James', 4.40, 43, 52, 36, 76, 69, 87, 46, 1),
    (UUID(), @scarletsClub, @wingId, 'Ellis Mee', 4.40, 45, 45, 72, 72, 67, 79, 48, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Elrigh Louw', 4.40, 48, 52, 15, 50, 66, 80, 67, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Jeandre Rudolph', 4.40, 55, 64, 13, 47, 71, 71, 64, 1),
    (UUID(), @zebreParmaClub, @wingId, 'Malik Faissal', 4.40, 53, 53, 46, 90, 55, 69, 46, 1),
    (UUID(), @ospreysClub, @scrumhalfId, 'Reuben Morgan-Williams', 4.40, 59, 38, 44, 83, 59, 68, 60, 1),
    (UUID(), @glasgowWarriorsClub, @looseForwardId, 'Rory Darge', 4.40, 46, 70, 28, 62, 59, 73, 52, 1),
    (UUID(), @munsterClub, @scrumhalfId, 'Craig Casey', 4.30, 54, 45, 54, 47, 67, 85, 51, 1),
    (UUID(), @munsterClub, @looseForwardId, 'John Hodnett', 4.30, 66, 62, 17, 62, 51, 58, 58, 1),
    (UUID(), @scarletsClub, @flyhalfId, 'Sam Costelow', 4.30, 54, 50, 67, 68, 54, 74, 51, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Caelan Doris', 4.20, 56, 68, 26, 61, 50, 65, 54, 1),
    (UUID(), @cardiffClub, @wingId, 'Josh Adams', 4.20, 47, 51, 40, 65, 68, 83, 57, 1),
    (UUID(), @leinsterClub, @wingId, 'Joshua Kenny', 4.20, 58, 40, 30, 70, 61, 74, 62, 1),
    (UUID(), @ospreysClub, @scrumhalfId, 'Kieran Hardy', 4.20, 62, 55, 57, 66, 55, 61, 50, 1),
    (UUID(), @zebreParmaClub, @lockId, 'Leonard Krumov', 4.20, 35, 43, 16, 63, 84, 90, 64, 1),
    (UUID(), @benettonClub, @propId, 'Mirco Spagnolo', 4.20, 57, 66, 26, 59, 52, 58, 58, 1),
    (UUID(), @edinburghClub, @propId, 'Pierre Schoeman', 4.20, 47, 63, 18, 58, 67, 69, 69, 1),
    (UUID(), @benettonClub, @fullbackId, 'Rhyno Smith', 4.20, 62, 44, 57, 66, 62, 76, 64, 1),
    (UUID(), @zebreParmaClub, @looseForwardId, 'Samuele Locatelli', 4.20, 37, 67, 25, 31, 68, 76, 65, 1),
    (UUID(), @ospreysClub, @wingId, 'Daniel Kasende', 4.10, 41, 52, 18, 37, 76, 90, 68, 1),
    (UUID(), @sharksClub, @scrumhalfId, 'Grant Williams', 4.10, 64, 48, 46, 52, 60, 67, 57, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Josh Macleod', 4.10, 42, 63, 16, 44, 66, 81, 58, 1),
    (UUID(), @zebreParmaClub, @centreId, 'Marco Zanon', 4.10, 50, 60, 13, 51, 63, 70, 61, 1),
    (UUID(), @stormersClub, @lockId, 'Ruben van Heerden', 4.10, 54, 53, 22, 66, 60, 63, 63, 1),
    (UUID(), @bullsClub, @centreId, 'Stravino Jacobs', 4.10, 63, 17, 55, 68, 68, 80, 60, 1),
    (UUID(), @glasgowWarriorsClub, @flyhalfId, 'Adam Hastings', 4.00, 57, 55, 49, 20, 60, 72, 61, 1),
    (UUID(), @stormersClub, @lockId, 'Adre Smith', 4.00, 56, 54, 6, 31, 72, 75, 60, 1),
    (UUID(), @glasgowWarriorsClub, @looseForwardId, 'Euan Ferrie', 4.00, 40, 58, 18, 64, 69, 78, 54, 1),
    (UUID(), @sharksClub, @lockId, 'Jason Jenkins', 4.00, 53, 47, 8, 57, 66, 78, 67, 1),
    (UUID(), @leinsterClub, @propId, 'Thomas Clarkson', 4.00, 62, 57, 10, 35, 61, 65, 63, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'Dan Thomas', 3.90, 43, 65, 27, 50, 68, 78, 60, 1),
    (UUID(), @connachtClub, @lockId, 'Darragh Murray', 3.90, 27, 57, 23, 75, 72, 86, 53, 1),
    (UUID(), @lionsClub, @lockId, 'Darrien Landsberg', 3.90, 71, 48, 26, 56, 51, 62, 58, 1),
    (UUID(), @edinburghClub, @fullbackId, 'Harry Paterson', 3.90, 64, 41, 48, 46, 56, 77, 55, 1),
    (UUID(), @ulsterClub, @lockId, 'Harry Sheridan', 3.90, 51, 59, 7, 37, 58, 73, 68, 1),
    (UUID(), @connachtClub, @flyhalfId, 'Harry West', 3.90, 64, 50, 25, 64, 58, 69, 50, 1),
    (UUID(), @stormersClub, @lockId, 'JD Schickerling', 3.90, 45, 47, 12, 76, 78, 81, 55, 1),
    (UUID(), @leinsterClub, @lockId, 'James Ryan', 3.90, 40, 51, 26, 60, 71, 83, 54, 1),
    (UUID(), @cardiffClub, @hookerId, 'Liam Belcher', 3.90, 41, 67, 19, 45, 70, 73, 57, 1),
    (UUID(), @ospreysClub, @looseForwardId, 'Morgan Morris', 3.90, 49, 64, 23, 80, 51, 60, 58, 1),
    (UUID(), @scarletsClub, @lockId, 'Sam Lousi', 3.90, 50, 64, 25, 52, 80, 80, 64, 1),
    (UUID(), @zebreParmaClub, @looseForwardId, 'Davide Odiase', 3.80, 45, 68, 26, 39, 64, 69, 54, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'James Botham', 3.80, 50, 58, 16, 55, 60, 71, 58, 1),
    (UUID(), @glasgowWarriorsClub, @hookerId, 'Johnny Matthews', 3.80, 69, 58, 11, 80, 56, 61, 59, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Josh van der Flier', 3.80, 61, 54, 11, 72, 63, 69, 65, 1),
    (UUID(), @connachtClub, @flyhalfId, 'Sean Naughton', 3.80, 55, 24, 49, 80, 67, 73, 57, 1),
    (UUID(), @benettonClub, @looseForwardId, 'Alessandro Izekor', 3.70, 26, 56, 14, 72, 77, 85, 53, 1),
    (UUID(), @edinburghClub, @wingId, 'Duhan van der Merwe', 3.70, 63, 36, 25, 84, 63, 76, 52, 1),
    (UUID(), @scarletsClub, @propId, 'Kemsley Mathias', 3.70, 41, 58, 16, 43, 67, 77, 62, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Liam McConnell', 3.70, 40, 64, 18, 45, 62, 73, 61, 1),
    (UUID(), @benettonClub, @looseForwardId, 'Lorenzo Cannone', 3.70, 39, 61, 24, 67, 57, 71, 61, 1),
    (UUID(), @ulsterClub, @fullbackId, 'Mike Lowry', 3.70, 73, 33, 31, 52, 64, 75, 48, 1),
    (UUID(), @ospreysClub, @looseForwardId, 'Morgan Morse', 3.70, 65, 60, 25, 38, 47, 58, 59, 1),
    (UUID(), @ulsterClub, @propId, 'Scott Wilson', 3.70, 60, 58, 21, 45, 55, 60, 62, 1),
    (UUID(), @lionsClub, @lockId, 'Etienne Oosthuizen', 3.60, 62, 55, 18, 49, 65, 71, 63, 1),
    (UUID(), @stormersClub, @flyhalfId, 'Jurie Matthee', 3.60, 63, 43, 63, 61, 51, 60, 54, 1),
    (UUID(), @leinsterClub, @lockId, 'RG Snyman', 3.60, 74, 50, 25, 80, 47, 57, 53, 1),
    (UUID(), @leinsterClub, @centreId, 'Robbie Henshaw', 3.60, 49, 49, 48, 85, 62, 70, 62, 1),
    (UUID(), @glasgowWarriorsClub, @lockId, 'Alex Craig', 3.50, 52, 66, 12, 51, 61, 60, 54, 1),
    (UUID(), @dragonsClub, @hookerId, 'Brodie Coghlan', 3.50, 38, 65, 19, 34, 66, 74, 61, 1),
    (UUID(), @bullsClub, @wingId, 'Cheswill Jooste', 3.50, 68, 44, 40, 66, 44, 57, 58, 1),
    (UUID(), @benettonClub, @lockId, 'Federico Ruzza', 3.50, 59, 61, 14, 68, 55, 56, 60, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Jarod Cairns', 3.50, 50, 60, 19, 67, 54, 68, 50, 1),
    (UUID(), @sharksClub, @centreId, 'Litelihle Bester', 3.50, 65, 48, 58, 80, 44, 60, 45, 1),
    (UUID(), @connachtClub, @wingId, 'Sam Gilbert', 3.50, 44, 28, 74, 64, 73, 89, 46, 1),
    (UUID(), @sharksClub, @wingId, 'Zekethelo Siyaya', 3.50, 72, 33, 72, 61, 54, 69, 39, 1),
    (UUID(), @edinburghClub, @propId, 'Boan Venter', 3.40, 73, 55, 17, 43, 51, 54, 55, 1),
    (UUID(), @bullsClub, @lockId, 'Cobus Wiese', 3.40, 62, 49, 22, 38, 49, 63, 71, 1),
    (UUID(), @zebreParmaClub, @centreId, 'Damiano Mazza', 3.40, 28, 57, 22, 59, 73, 80, 55, 1),
    (UUID(), @sharksClub, @centreId, 'Ethan Hooker', 3.40, 55, 31, 18, 62, 70, 85, 52, 1),
    (UUID(), @glasgowWarriorsClub, @wingId, 'Fergus Watson', 3.40, 57, 49, 37, 65, 51, 70, 49, 1),
    (UUID(), @leinsterClub, @hookerId, 'Gus McCarthy', 3.40, 54, 53, 18, 84, 51, 53, 62, 1),
    (UUID(), @ospreysClub, @looseForwardId, 'Jac Morgan', 3.40, 56, 61, 16, 53, 54, 70, 50, 1),
    (UUID(), @zebreParmaClub, @lockId, 'Matteo Canali', 3.40, 33, 64, 8, 72, 64, 76, 51, 1),
    (UUID(), @edinburghClub, @centreId, 'Piers O''Conor', 3.40, 39, 41, 67, 81, 66, 74, 49, 1),
    (UUID(), @ulsterClub, @centreId, 'Stuart McCloskey', 3.40, 52, 63, 39, 50, 66, 70, 55, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Alex Kendellen', 3.30, 50, 62, 27, 50, 52, 63, 58, 1),
    (UUID(), @lionsClub, @wingId, 'Angelo Davids', 3.30, 37, 32, 8, 78, 77, 95, 52, 1),
    (UUID(), @cardiffClub, @lockId, 'George Nott', 3.30, 25, 54, 17, 82, 74, 76, 59, 1),
    (UUID(), @dragonsClub, @looseForwardId, 'Harrison Keddie', 3.30, 45, 64, 10, 48, 58, 67, 63, 1),
    (UUID(), @munsterClub, @lockId, 'Tadhg Beirne', 3.30, 59, 62, 23, 66, 58, 68, 56, 1),
    (UUID(), @zebreParmaClub, @scrumhalfId, 'Alessandro Fusco', 3.20, 46, 44, 52, 27, 68, 84, 52, 1),
    (UUID(), @glasgowWarriorsClub, @hookerId, 'Angus Fraser', 3.20, 46, 62, 29, 72, 52, 60, 53, 1),
    (UUID(), @ospreysClub, @centreId, 'Evardi Boshoff', 3.20, 57, 52, 39, 48, 49, 60, 59, 1),
    (UUID(), @leinsterClub, @fullbackId, 'Hugo Keenan', 3.20, 69, 29, 42, 90, 50, 70, 49, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Jack Conan', 3.20, 60, 53, 18, 75, 63, 68, 53, 1),
    (UUID(), @leinsterClub, @wingId, 'James Lowe', 3.20, 64, 32, 81, 42, 63, 82, 62, 1),
    (UUID(), @glasgowWarriorsClub, @looseForwardId, 'Macenzzie Duncan', 3.20, 55, 48, 23, 70, 51, 63, 59, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Max Deegan', 3.20, 46, 44, 6, 26, 73, 81, 65, 1),
    (UUID(), @lionsClub, @scrumhalfId, 'Nico Steyn', 3.20, 62, 49, 40, 42, 55, 65, 51, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Renzo du Plessis', 3.20, 66, 65, 21, 74, 34, 39, 60, 1),
    (UUID(), @dragonsClub, @propId, 'Rob Hunt', 3.20, 46, 64, 20, 51, 55, 60, 59, 1),
    (UUID(), @leinsterClub, @lockId, 'Ryan Baird', 3.20, 50, 59, 42, 61, 49, 78, 38, 1),
    (UUID(), @dragonsClub, @lockId, 'Seb Davies', 3.20, 42, 55, 22, 75, 63, 74, 49, 1),
    (UUID(), @glasgowWarriorsClub, @hookerId, 'Gregor Hiddleston', 3.10, 45, 66, 24, 37, 55, 59, 63, 1),
    (UUID(), @leinsterClub, @wingId, 'Jordan Larmour', 3.10, 65, 50, 39, 50, 48, 67, 47, 1),
    (UUID(), @cardiffClub, @centreId, 'Mason Grady', 3.10, 62, 30, 28, 76, 59, 81, 45, 1),
    (UUID(), @glasgowWarriorsClub, @lockId, 'Max Williamson', 3.10, 52, 51, 26, 72, 54, 61, 54, 1),
    (UUID(), @glasgowWarriorsClub, @hookerId, 'Seb Stephen', 3.10, 67, 54, 17, 74, 42, 41, 59, 1),
    (UUID(), @stormersClub, @propId, 'Vernon Matongo', 3.10, 53, 56, 24, 71, 56, 56, 50, 1),
    (UUID(), @dragonsClub, @wingId, 'Dai Richards', 3.00, 38, 43, 12, 78, 63, 82, 56, 1),
    (UUID(), @connachtClub, @hookerId, 'Dave Heffernan', 3.00, 58, 61, 25, 79, 55, 61, 65, 1),
    (UUID(), @ospreysClub, @hookerId, 'Dewi Lake', 3.00, 45, 67, 23, 21, 57, 70, 58, 1),
    (UUID(), @munsterClub, @wingId, 'Diarmuid Kilgallen', 3.00, 58, 50, 38, 73, 49, 70, 39, 1),
    (UUID(), @connachtClub, @propId, 'Jack Aungier', 3.00, 64, 57, 24, 59, 51, 53, 46, 1),
    (UUID(), @glasgowWarriorsClub, @looseForwardId, 'Jack Dempsey', 3.00, 62, 47, 25, 49, 64, 72, 51, 1),
    (UUID(), @scarletsClub, @centreId, 'Joe Hawkins', 3.00, 30, 40, 56, 87, 66, 80, 48, 1),
    (UUID(), @edinburghClub, @lockId, 'Marshall Sykes', 3.00, 23, 55, 8, 88, 71, 81, 48, 1),
    (UUID(), @munsterClub, @propId, 'Michael Milne', 3.00, 52, 64, 24, 42, 53, 58, 57, 1),
    (UUID(), @benettonClub, @wingId, 'Onisi Ratave', 3.00, 70, 53, 30, 31, 58, 71, 60, 1),
    (UUID(), @cardiffClub, @hookerId, 'Evan Lloyd', 2.90, 66, 65, 21, 75, 38, 39, 51, 1),
    (UUID(), @bullsClub, @propId, 'Gerhard Steenekamp', 2.90, 34, 51, 25, 73, 69, 73, 48, 1),
    (UUID(), @edinburghClub, @lockId, 'Grant Gilchrist', 2.90, 40, 62, 19, 50, 79, 80, 59, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Jack O''Donoghue', 2.90, 46, 60, 15, 31, 66, 70, 65, 1),
    (UUID(), @benettonClub, @lockId, 'Jadin Kingi', 2.90, 52, 64, 14, 77, 47, 49, 53, 1),
    (UUID(), @stormersClub, @wingId, 'Leolin Zas', 2.90, 32, 30, 44, 58, 81, 94, 52, 1),
    (UUID(), @dragonsClub, @looseForwardId, 'Ryan Woodman', 2.90, 15, 53, 24, 68, 79, 85, 51, 1),
    (UUID(), @dragonsClub, @looseForwardId, 'Thomas Young', 2.90, 59, 64, 21, 39, 60, 63, 62, 1),
    (UUID(), @zebreParmaClub, @lockId, 'Alessandro Ortombina', 2.80, 20, 60, 15, 82, 70, 76, 48, 1),
    (UUID(), @edinburghClub, @scrumhalfId, 'Ben Vellacott', 2.80, 61, 19, 56, 75, 65, 69, 57, 1),
    (UUID(), @munsterClub, @wingId, 'Calvin Nash', 2.80, 45, 44, 38, 82, 57, 76, 42, 1),
    (UUID(), @leinsterClub, @hookerId, 'Dan Sheehan', 2.80, 68, 43, 20, 64, 44, 57, 57, 1),
    (UUID(), @edinburghClub, @hookerId, 'Dylan Richardson', 2.80, 56, 63, 11, 30, 55, 62, 54, 1),
    (UUID(), @munsterClub, @lockId, 'Evan O''Connell', 2.80, 50, 58, 23, 85, 52, 62, 40, 1),
    (UUID(), @sharksClub, @wingId, 'Jaco Williams', 2.80, 51, 31, 53, 90, 54, 69, 47, 1),
    (UUID(), @munsterClub, @lockId, 'Jean Kleyn', 2.80, 41, 62, 21, 64, 64, 67, 54, 1),
    (UUID(), @bullsClub, @scrumhalfId, 'Keagan Johannes', 2.80, 57, 48, 48, 73, 46, 64, 45, 1),
    (UUID(), @connachtClub, @scrumhalfId, 'Matthew Devine', 2.80, 65, 54, 46, 86, 35, 38, 52, 1),
    (UUID(), @connachtClub, @lockId, 'Niall Murray', 2.80, 47, 65, 18, 55, 50, 54, 57, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Nick Hatton', 2.80, 43, 53, 23, 70, 57, 61, 55, 1),
    (UUID(), @sharksClub, @propId, 'Ox Nche', 2.80, 54, 49, 10, 80, 53, 62, 59, 1),
    (UUID(), @ospreysClub, @centreId, 'Phil Cokanasiga', 2.80, 57, 37, 64, 82, 52, 60, 44, 1),
    (UUID(), @zebreParmaClub, @hookerId, 'Tommaso di Bartolomeo', 2.80, 29, 56, 16, 65, 65, 75, 55, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'Alex Mann', 2.70, 49, 62, 49, 21, 51, 68, 51, 1),
    (UUID(), @sharksClub, @fullbackId, 'Aphelele Fassi', 2.70, 57, 31, 75, 50, 52, 80, 44, 1),
    (UUID(), @connachtClub, @centreId, 'Bundee Aki', 2.70, 50, 60, 45, 52, 69, 70, 57, 1),
    (UUID(), @bullsClub, @lockId, 'JJ Theron', 2.70, 45, 57, 15, 80, 55, 67, 40, 1),
    (UUID(), @munsterClub, @propId, 'Jeremy Loughman', 2.70, 45, 70, 14, 49, 56, 61, 54, 1),
    (UUID(), @connachtClub, @lockId, 'Joe Joyce', 2.70, 61, 56, 23, 28, 65, 62, 55, 1);

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (UUID(), @sharksClub, @flyhalfId, 'Jordan Hendrikse', 2.70, 47, 43, 49, 27, 67, 81, 49, 1),
    (UUID(), @connachtClub, @wingId, 'Mack Hansen', 2.70, 60, 39, 56, 79, 48, 74, 33, 1),
    (UUID(), @lionsClub, @hookerId, 'PJ Botha', 2.70, 36, 55, 25, 65, 61, 75, 48, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Ben Muncaster', 2.60, 48, 55, 28, 66, 49, 62, 51, 1),
    (UUID(), @connachtClub, @centreId, 'Byron Ralston', 2.60, 60, 49, 51, 53, 48, 59, 44, 1),
    (UUID(), @dragonsClub, @looseForwardId, 'Evan Minto', 2.60, 55, 54, 38, 59, 47, 67, 39, 1),
    (UUID(), @leinsterClub, @flyhalfId, 'Harry Byrne', 2.60, 42, 38, 64, 70, 57, 67, 51, 1),
    (UUID(), @glasgowWarriorsClub, @centreId, 'Johnny Ventisei', 2.60, 49, 56, 46, 76, 47, 58, 40, 1),
    (UUID(), @munsterClub, @hookerId, 'Lee Barron', 2.60, 45, 66, 17, 45, 51, 52, 61, 1),
    (UUID(), @benettonClub, @scrumhalfId, 'Louis Werchon', 2.60, 51, 52, 59, 86, 45, 46, 46, 1),
    (UUID(), @glasgowWarriorsClub, @lockId, 'Olujare Oguntibeju', 2.60, 65, 56, 13, 53, 47, 53, 50, 1),
    (UUID(), @glasgowWarriorsClub, @propId, 'Zander Fagerson', 2.60, 48, 58, 16, 57, 55, 58, 55, 1),
    (UUID(), @ospreysClub, @propId, 'Garyn Phillips', 2.50, 58, 64, 14, 59, 44, 48, 51, 1),
    (UUID(), @munsterClub, @flyhalfId, 'JJ Hanrahan', 2.50, 26, 55, 52, 67, 65, 73, 60, 1),
    (UUID(), @cardiffClub, @propId, 'Javan Sebastian', 2.50, 48, 54, 7, 42, 65, 65, 58, 1),
    (UUID(), @edinburghClub, @wingId, 'Lewis Wells', 2.50, 56, 42, 44, 83, 45, 57, 47, 1),
    (UUID(), @connachtClub, @propId, 'Sam Illo', 2.50, 49, 68, 14, 43, 53, 56, 50, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Brian Deeny', 2.40, 20, 65, 7, 35, 70, 81, 57, 1),
    (UUID(), @connachtClub, @scrumhalfId, 'Caolin Blade', 2.40, 53, 42, 52, 89, 57, 65, 39, 1),
    (UUID(), @glasgowWarriorsClub, @centreId, 'Duncan Munn', 2.40, 50, 40, 54, 78, 46, 73, 41, 1),
    (UUID(), @dragonsClub, @lockId, 'Levi Douglas', 2.40, 63, 56, 18, 24, 55, 55, 55, 1),
    (UUID(), @cardiffClub, @centreId, 'Steff Emanuel', 2.40, 51, 48, 60, 85, 47, 52, 41, 1),
    (UUID(), @sharksClub, @flyhalfId, 'Vusi Moyo', 2.40, 53, 45, 70, 77, 47, 53, 40, 1),
    (UUID(), @bullsClub, @fullbackId, 'Willie le Roux', 2.40, 58, 30, 67, 45, 72, 70, 66, 1),
    (UUID(), @edinburghClub, @flyhalfId, 'Ben Healy', 2.30, 39, 46, 72, 66, 48, 76, 41, 1),
    (UUID(), @connachtClub, @propId, 'Fiachna Barrett', 2.30, 60, 62, 17, 66, 41, 39, 51, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Freddy Douglas', 2.30, 31, 64, 21, 40, 61, 64, 55, 1),
    (UUID(), @bullsClub, @centreId, 'Jan Serfontein', 2.30, 54, 62, 37, 83, 47, 55, 43, 1),
    (UUID(), @benettonClub, @flyhalfId, 'Leonardo Marin', 2.30, 55, 49, 26, 73, 47, 62, 44, 1),
    (UUID(), @scarletsClub, @hookerId, 'Marnus van der Merwe', 2.30, 34, 60, 24, 45, 55, 64, 59, 1),
    (UUID(), @benettonClub, @flyhalfId, 'Nicolas Roger Farias', 2.30, 38, 50, 49, 83, 52, 76, 32, 1),
    (UUID(), @bullsClub, @centreId, 'Stedman Gans', 2.30, 59, 43, 46, 42, 52, 57, 53, 1),
    (UUID(), @ulsterClub, @centreId, 'Wilhelm de Klerk', 2.30, 51, 57, 46, 75, 46, 50, 39, 1),
    (UUID(), @connachtClub, @propId, 'Billy Bohan', 2.20, 43, 67, 18, 59, 51, 52, 44, 1),
    (UUID(), @stormersClub, @centreId, 'Dan du Plessis', 2.20, 30, 46, 36, 60, 73, 79, 45, 1),
    (UUID(), @benettonClub, @lockId, 'Eli Snyman', 2.20, 19, 56, 22, 86, 66, 75, 45, 1),
    (UUID(), @ulsterClub, @wingId, 'Ethan Mcilroy', 2.20, 44, 48, 18, 69, 56, 70, 44, 1),
    (UUID(), @edinburghClub, @centreId, 'Findlay Thomson', 2.20, 38, 53, 31, 56, 60, 80, 37, 1),
    (UUID(), @dragonsClub, @looseForwardId, 'Harry Beddall', 2.20, 24, 72, 12, 53, 61, 66, 46, 1),
    (UUID(), @ulsterClub, @flyhalfId, 'Jack Murphy', 2.20, 28, 34, 50, 44, 74, 88, 50, 1),
    (UUID(), @leinsterClub, @scrumhalfId, 'Luke McGrath', 2.20, 51, 44, 53, 52, 56, 61, 61, 1),
    (UUID(), @edinburghClub, @centreId, 'Mosese Tuipulotu', 2.20, 38, 47, 18, 80, 63, 76, 37, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Mpilo Gumede', 2.20, 56, 54, 19, 73, 47, 55, 43, 1),
    (UUID(), @bullsClub, @scrumhalfId, 'Paul de Wet', 2.20, 69, 40, 36, 60, 47, 41, 57, 1),
    (UUID(), @dragonsClub, @lockId, 'Shane Lewis-Hughes', 2.20, 41, 63, 20, 46, 51, 55, 58, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Tom Currie', 2.20, 61, 56, 21, 63, 45, 41, 48, 1),
    (UUID(), @sharksClub, @wingId, 'Yaw Penxe', 2.20, 51, 36, 60, 78, 52, 68, 37, 1),
    (UUID(), @glasgowWarriorsClub, @lockId, 'Alex Samuel', 2.10, 37, 42, 17, 49, 68, 73, 54, 1),
    (UUID(), @scarletsClub, @scrumhalfId, 'Archie Hughes', 2.10, 46, 57, 55, 74, 43, 44, 46, 1),
    (UUID(), @scarletsClub, @flyhalfId, 'Carwyn Leggatt-Jones', 2.10, 58, 56, 59, 38, 41, 52, 43, 1),
    (UUID(), @zebreParmaClub, @lockId, 'Franco Carrera', 2.10, 66, 59, 14, 27, 46, 55, 46, 1),
    (UUID(), @zebreParmaClub, @looseForwardId, 'Giovanni Licata', 2.10, 58, 55, 31, 26, 49, 54, 52, 1),
    (UUID(), @connachtClub, @centreId, 'Hugh Gavin', 2.10, 51, 45, 42, 67, 50, 63, 41, 1),
    (UUID(), @leinsterClub, @propId, 'Jerry Cahir', 2.10, 40, 57, 17, 69, 49, 55, 55, 1),
    (UUID(), @zebreParmaClub, @fullbackId, 'Lorenzo Pani', 2.10, 41, 33, 68, 81, 56, 77, 35, 1),
    (UUID(), @edinburghClub, @wingId, 'Malelili Satala', 2.10, 48, 36, 39, 54, 60, 78, 42, 1),
    (UUID(), @benettonClub, @hookerId, 'Nicholas Gasperini', 2.10, 58, 60, 23, 71, 37, 42, 49, 1),
    (UUID(), @leinsterClub, @wingId, 'Ciaran Mangan', 2.00, 54, 44, 43, 81, 49, 54, 35, 1),
    (UUID(), @cardiffClub, @hookerId, 'Dafydd Hughes', 2.00, 54, 58, 18, 64, 38, 43, 58, 1),
    (UUID(), @sharksClub, @propId, 'Hanro Jacobs', 2.00, 49, 47, 22, 48, 57, 61, 49, 1),
    (UUID(), @dragonsClub, @wingId, 'Jared Rosser', 2.00, 44, 32, 23, 72, 60, 77, 47, 1),
    (UUID(), @leinsterClub, @hookerId, 'Ronan Kelleher', 2.00, 55, 53, 15, 38, 50, 62, 50, 1),
    (UUID(), @scarletsClub, @hookerId, 'Ryan Elias', 2.00, 35, 49, 27, 49, 67, 70, 54, 1),
    (UUID(), @ulsterClub, @propId, 'Tom McAllister', 2.00, 53, 61, 20, 68, 46, 44, 41, 1),
    (UUID(), @ulsterClub, @wingId, 'Aitzol Arenzana-King', 1.90, 58, 42, 47, 62, 47, 74, 29, 1),
    (UUID(), @benettonClub, @scrumhalfId, 'Alessandro Garbisi', 1.90, 54, 47, 36, 42, 50, 62, 47, 1),
    (UUID(), @glasgowWarriorsClub, @scrumhalfId, 'Ben Afshar', 1.90, 45, 41, 53, 91, 47, 55, 42, 1),
    (UUID(), @munsterClub, @fullbackId, 'Ben O''Connor', 1.90, 56, 30, 51, 83, 50, 73, 27, 1),
    (UUID(), @edinburghClub, @scrumhalfId, 'Charlie Shiel', 1.90, 64, 47, 61, 58, 41, 41, 42, 1),
    (UUID(), @dragonsClub, @scrumhalfId, 'Che Hope', 1.90, 49, 43, 53, 74, 46, 58, 44, 1),
    (UUID(), @benettonClub, @centreId, 'Federico Zanandrea', 1.90, 50, 45, 46, 79, 46, 52, 41, 1),
    (UUID(), @zebreParmaClub, @looseForwardId, 'Giacomo Ferrari', 1.90, 41, 58, 18, 34, 52, 62, 56, 1),
    (UUID(), @stormersClub, @hookerId, 'JJ Kotze', 1.90, 44, 51, 19, 72, 46, 52, 56, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'James McKillop', 1.90, 45, 59, 18, 71, 46, 51, 45, 1),
    (UUID(), @cardiffClub, @lockId, 'Josh McNally', 1.90, 38, 51, 15, 23, 85, 83, 53, 1),
    (UUID(), @glasgowWarriorsClub, @centreId, 'Kerr Yule', 1.90, 39, 50, 42, 70, 52, 58, 43, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Marco van Staden', 1.90, 56, 57, 21, 43, 41, 46, 60, 1),
    (UUID(), @bullsClub, @lockId, 'Nicolaas Janse van Rensburg', 1.90, 56, 62, 28, 64, 41, 52, 44, 1),
    (UUID(), @edinburghClub, @propId, 'Paul Hill', 1.90, 38, 59, 9, 60, 58, 60, 52, 1),
    (UUID(), @dragonsClub, @scrumhalfId, 'Rhodri Williams', 1.90, 39, 51, 53, 73, 51, 58, 55, 1),
    (UUID(), @ospreysClub, @looseForwardId, 'Ross Moriarty', 1.90, 45, 46, 8, 36, 61, 70, 65, 1),
    (UUID(), @munsterClub, @wingId, 'Andrew Smith', 1.80, 48, 45, 51, 33, 49, 72, 47, 1),
    (UUID(), @edinburghClub, @flyhalfId, 'Cammy Scott', 1.80, 40, 62, 53, 68, 41, 48, 45, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Connor Boyle', 1.80, 43, 63, 24, 80, 42, 48, 41, 1),
    (UUID(), @connachtClub, @lockId, 'David O''Connor', 1.80, 42, 58, 19, 74, 57, 53, 44, 1),
    (UUID(), @munsterClub, @lockId, 'Edwin Edogbo', 1.80, 49, 63, 19, 60, 43, 46, 46, 1),
    (UUID(), @benettonClub, @looseForwardId, 'Giulio Marini', 1.80, 27, 55, 25, 68, 54, 63, 53, 1),
    (UUID(), @edinburghClub, @scrumhalfId, 'Hector Patterson', 1.80, 57, 28, 55, 51, 49, 67, 46, 1),
    (UUID(), @leinsterClub, @centreId, 'Hugh Cooney', 1.80, 53, 40, 43, 77, 45, 58, 41, 1),
    (UUID(), @leinsterClub, @propId, 'Jack Boyle', 1.80, 54, 59, 26, 30, 43, 48, 55, 1),
    (UUID(), @bullsClub, @propId, 'Jan-Hendrik Wessels', 1.80, 51, 47, 43, 47, 51, 55, 48, 1),
    (UUID(), @benettonClub, @looseForwardId, 'Manuel Zuliani', 1.80, 43, 61, 22, 39, 50, 59, 48, 1),
    (UUID(), @zebreParmaClub, @propId, 'Muhamed Hasa', 1.80, 39, 46, 9, 53, 62, 71, 49, 1),
    (UUID(), @sharksClub, @propId, 'Phatu Ganyane', 1.80, 38, 58, 17, 56, 55, 53, 50, 1),
    (UUID(), @ospreysClub, @propId, 'Rhys Henry', 1.80, 50, 54, 21, 61, 48, 49, 47, 1),
    (UUID(), @cardiffClub, @lockId, 'Rory Thornton', 1.80, 34, 62, 24, 64, 57, 57, 46, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Ruadhan Quinn', 1.80, 46, 56, 19, 74, 46, 48, 48, 1),
    (UUID(), @ospreysClub, @hookerId, 'Sam Parry', 1.80, 34, 63, 24, 55, 61, 58, 59, 1),
    (UUID(), @benettonClub, @hookerId, 'Siua Maile', 1.80, 42, 51, 17, 62, 48, 62, 53, 1),
    (UUID(), @stormersClub, @propId, 'Zac Porthen', 1.80, 43, 66, 18, 67, 50, 45, 42, 1),
    (UUID(), @glasgowWarriorsClub, @flyhalfId, 'Charlie Savala', 1.70, 55, 44, 76, 72, 48, 32, 41, 1),
    (UUID(), @connachtClub, @wingId, 'Chay Mullins', 1.70, 37, 50, 37, 26, 61, 77, 44, 1),
    (UUID(), @munsterClub, @hookerId, 'Diarmuid Barron', 1.70, 30, 55, 27, 53, 58, 66, 46, 1),
    (UUID(), @stormersClub, @wingId, 'Dylan Maart', 1.70, 48, 39, 46, 49, 58, 74, 41, 1),
    (UUID(), @connachtClub, @hookerId, 'Dylan Tierney-Martin', 1.70, 52, 55, 23, 38, 47, 56, 47, 1),
    (UUID(), @connachtClub, @hookerId, 'Eoin de Buitlear', 1.70, 45, 65, 28, 65, 46, 47, 39, 1),
    (UUID(), @scarletsClub, @centreId, 'Gabe McDonald', 1.70, 54, 52, 45, 72, 45, 30, 45, 1),
    (UUID(), @sharksClub, @centreId, 'Le Roux Malan', 1.70, 54, 41, 46, 85, 46, 53, 35, 1),
    (UUID(), @zebreParmaClub, @flyhalfId, 'Martin Roger Farias', 1.70, 33, 42, 46, 57, 58, 73, 47, 1),
    (UUID(), @sharksClub, @lockId, 'Marvin Orie', 1.70, 33, 62, 14, 67, 58, 66, 49, 1),
    (UUID(), @dragonsClub, @hookerId, 'Oli Burrows', 1.70, 40, 66, 22, 68, 43, 43, 51, 1),
    (UUID(), @lionsClub, @propId, 'RF Schoeman', 1.70, 36, 61, 20, 57, 51, 48, 55, 1),
    (UUID(), @lionsClub, @lockId, 'Reinhard Nothnagel', 1.70, 24, 54, 21, 84, 60, 64, 43, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Sean Edogbo', 1.70, 44, 49, 25, 69, 54, 64, 36, 1),
    (UUID(), @connachtClub, @looseForwardId, 'Sean O''Brien', 1.70, 44, 59, 16, 57, 47, 50, 51, 1),
    (UUID(), @lionsClub, @wingId, 'Tapiwa Mafura', 1.70, 46, 45, 48, 78, 44, 58, 42, 1),
    (UUID(), @ulsterClub, @propId, 'Tom O''Toole', 1.70, 34, 64, 12, 36, 57, 59, 51, 1),
    (UUID(), @benettonClub, @centreId, 'Filippo Drago', 1.60, 38, 55, 62, 74, 40, 49, 44, 1),
    (UUID(), @munsterClub, @centreId, 'Fionn Gibbons', 1.60, 55, 50, 41, 70, 48, 32, 44, 1),
    (UUID(), @dragonsClub, @hookerId, 'George Roberts', 1.60, 44, 59, 33, 68, 48, 47, 39, 1),
    (UUID(), @sharksClub, @fullbackId, 'Hakeem Kunene', 1.60, 58, 53, 41, 60, 38, 34, 50, 1),
    (UUID(), @scarletsClub, @fullbackId, 'Ioan Jones', 1.60, 52, 43, 53, 85, 42, 39, 43, 1),
    (UUID(), @lionsClub, @looseForwardId, 'JC Pretorius', 1.60, 56, 62, 20, 70, 38, 38, 42, 1),
    (UUID(), @lionsClub, @flyhalfId, 'Lubabalo Dobela', 1.60, 43, 49, 70, 80, 45, 51, 33, 1),
    (UUID(), @dragonsClub, @looseForwardId, 'Mackenzie Martin', 1.60, 44, 62, 21, 68, 44, 53, 40, 1),
    (UUID(), @ospreysClub, @fullbackId, 'Max Nagy', 1.60, 56, 53, 43, 34, 45, 50, 45, 1),
    (UUID(), @munsterClub, @wingId, 'Shay McCarthy', 1.60, 54, 39, 52, 78, 45, 61, 32, 1),
    (UUID(), @leinsterClub, @fullbackId, 'Tadhg Brophy', 1.60, 57, 45, 59, 72, 48, 29, 40, 1),
    (UUID(), @benettonClub, @centreId, 'Tomas Medina', 1.60, 48, 51, 58, 54, 47, 52, 37, 1),
    (UUID(), @connachtClub, @scrumhalfId, 'Albert Lindner', 1.50, 52, 46, 64, 72, 48, 28, 42, 1),
    (UUID(), @glasgowWarriorsClub, @looseForwardId, 'Ally Miller', 1.50, 39, 55, 19, 79, 48, 54, 45, 1),
    (UUID(), @ospreysClub, @propId, 'Ben Warren', 1.50, 34, 66, 21, 66, 47, 45, 47, 1),
    (UUID(), @dragonsClub, @fullbackId, 'Cai Evans', 1.50, 38, 41, 63, 88, 43, 54, 45, 1),
    (UUID(), @edinburghClub, @centreId, 'Charlie McCaig', 1.50, 55, 50, 37, 70, 48, 31, 43, 1),
    (UUID(), @ospreysClub, @wingId, 'Connor Moyse', 1.50, 58, 44, 45, 72, 48, 31, 44, 1),
    (UUID(), @leinsterClub, @centreId, 'Garry Ringrose', 1.50, 53, 34, 63, 88, 46, 60, 35, 1),
    (UUID(), @ospreysClub, @wingId, 'Harri Houston', 1.50, 63, 45, 41, 76, 44, 31, 40, 1),
    (UUID(), @edinburghClub, @hookerId, 'Harri Morris', 1.50, 40, 60, 28, 36, 49, 62, 44, 1),
    (UUID(), @ospreysClub, @scrumhalfId, 'Harri Williams', 1.50, 52, 46, 65, 72, 48, 31, 40, 1),
    (UUID(), @leinsterClub, @fullbackId, 'Henry McErlean', 1.50, 57, 45, 53, 72, 48, 31, 41, 1),
    (UUID(), @glasgowWarriorsClub, @centreId, 'Huw Jones', 1.50, 49, 37, 26, 73, 54, 70, 44, 1),
    (UUID(), @ulsterClub, @lockId, 'Iain Henderson', 1.50, 34, 61, 25, 20, 64, 65, 59, 1),
    (UUID(), @leinsterClub, @centreId, 'Jack Deegan', 1.50, 55, 50, 42, 70, 48, 28, 42, 1),
    (UUID(), @sharksClub, @scrumhalfId, 'Jaden Hendrikse', 1.50, 28, 53, 48, 53, 52, 58, 53, 1),
    (UUID(), @ulsterClub, @flyhalfId, 'Jake Flannery', 1.50, 56, 39, 52, 79, 37, 46, 45, 1),
    (UUID(), @leinsterClub, @scrumhalfId, 'Jamison Gibson-Park', 1.50, 61, 32, 56, 67, 54, 62, 42, 1),
    (UUID(), @stormersClub, @centreId, 'Jonathan Roche', 1.50, 43, 47, 25, 61, 53, 66, 37, 1),
    (UUID(), @bullsClub, @flyhalfId, 'Kade Wolhuter', 1.50, 55, 44, 67, 72, 48, 32, 40, 1),
    (UUID(), @ospreysClub, @fullbackId, 'Lewis Edwards', 1.50, 57, 45, 57, 72, 48, 27, 41, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Marco Ferreira', 1.50, 52, 58, 25, 66, 48, 28, 44, 1);

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (UUID(), @stormersClub, @propId, 'Neethling Fouche', 1.50, 33, 47, 16, 65, 61, 64, 58, 1),
    (UUID(), @benettonClub, @lockId, 'Niccolo Cannone', 1.50, 31, 61, 23, 45, 57, 62, 44, 1),
    (UUID(), @glasgowWarriorsClub, @lockId, 'Scott Cummings', 1.50, 41, 55, 20, 44, 57, 62, 41, 1),
    (UUID(), @benettonClub, @propId, 'Simone Ferrari', 1.50, 41, 62, 15, 68, 51, 60, 40, 1),
    (UUID(), @glasgowWarriorsClub, @looseForwardId, 'Sione Vailanu', 1.50, 54, 57, 26, 63, 42, 41, 48, 1),
    (UUID(), @benettonClub, @looseForwardId, 'So''otala Fa''aso''o', 1.50, 46, 50, 22, 61, 50, 58, 46, 1),
    (UUID(), @stormersClub, @scrumhalfId, 'Asad Moos', 1.40, 52, 46, 59, 72, 48, 30, 41, 1),
    (UUID(), @connachtClub, @scrumhalfId, 'Ben Murphy', 1.40, 38, 23, 52, 59, 63, 73, 48, 1),
    (UUID(), @scarletsClub, @centreId, 'Billy McBryde', 1.40, 55, 50, 42, 70, 48, 31, 40, 1),
    (UUID(), @glasgowWarriorsClub, @scrumhalfId, 'Callum Reidy', 1.40, 52, 46, 57, 72, 48, 29, 40, 1),
    (UUID(), @sharksClub, @centreId, 'Diego Appollis', 1.40, 55, 50, 46, 70, 48, 32, 37, 1),
    (UUID(), @munsterClub, @centreId, 'Eoghan Smyth', 1.40, 55, 50, 43, 70, 48, 29, 39, 1),
    (UUID(), @munsterClub, @scrumhalfId, 'Ethan Coughlan', 1.40, 38, 41, 53, 76, 45, 55, 47, 1),
    (UUID(), @connachtClub, @propId, 'Finlay Bealham', 1.40, 51, 63, 15, 39, 50, 50, 53, 1),
    (UUID(), @leinsterClub, @scrumhalfId, 'Fintan Gunne', 1.40, 60, 37, 55, 76, 40, 41, 41, 1),
    (UUID(), @scarletsClub, @scrumhalfId, 'Gareth Davies', 1.40, 46, 54, 66, 23, 53, 58, 58, 1),
    (UUID(), @munsterClub, @centreId, 'Gordon Wood', 1.40, 53, 50, 47, 72, 47, 29, 41, 1),
    (UUID(), @zebreParmaClub, @looseForwardId, 'Guido Volpi', 1.40, 32, 59, 21, 76, 49, 54, 45, 1),
    (UUID(), @scarletsClub, @fullbackId, 'Jac Davies', 1.40, 40, 43, 52, 48, 55, 68, 39, 1),
    (UUID(), @zebreParmaClub, @wingId, 'Jacopo Trulla', 1.40, 52, 39, 60, 84, 41, 50, 34, 1),
    (UUID(), @benettonClub, @centreId, 'Josh Flook', 1.40, 55, 50, 48, 70, 48, 32, 37, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Keanu Evans', 1.40, 48, 60, 27, 64, 48, 28, 45, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Marcel Theunissen', 1.40, 34, 48, 21, 52, 56, 57, 55, 1),
    (UUID(), @ulsterClub, @lockId, 'Matt Dalton', 1.40, 57, 55, 15, 75, 36, 42, 42, 1),
    (UUID(), @glasgowWarriorsClub, @flyhalfId, 'Matt Urwin', 1.40, 54, 43, 72, 72, 48, 29, 37, 1),
    (UUID(), @lionsClub, @hookerId, 'Morne Brandon', 1.40, 59, 53, 24, 43, 40, 39, 52, 1),
    (UUID(), @edinburghClub, @propId, 'Ollie Blyth-Lafferty', 1.40, 31, 63, 24, 65, 47, 42, 51, 1),
    (UUID(), @bullsClub, @centreId, 'Phillip-Albert van Niekerk', 1.40, 55, 50, 36, 70, 48, 30, 43, 1),
    (UUID(), @stormersClub, @wingId, 'Seabelo Senatla', 1.40, 52, 43, 67, 53, 48, 59, 43, 1),
    (UUID(), @connachtClub, @centreId, 'Sean Walsh', 1.40, 55, 50, 42, 70, 48, 27, 41, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'Taulupe Faletau', 1.40, 67, 55, 30, 73, 44, 35, 47, 1),
    (UUID(), @benettonClub, @flyhalfId, 'Tomas Albornoz', 1.40, 55, 44, 71, 72, 48, 32, 34, 1),
    (UUID(), @lionsClub, @lockId, 'WJ Steenkamp', 1.40, 48, 50, 19, 76, 47, 51, 37, 1),
    (UUID(), @benettonClub, @scrumhalfId, 'Andy Uren', 1.30, 25, 47, 58, 80, 55, 59, 39, 1),
    (UUID(), @stormersClub, @scrumhalfId, 'Cobus Reinach', 1.30, 57, 56, 43, 44, 49, 55, 44, 1),
    (UUID(), @cardiffClub, @centreId, 'Cornel Smit', 1.30, 54, 49, 47, 71, 46, 32, 37, 1),
    (UUID(), @connachtClub, @centreId, 'David Hawkshaw', 1.30, 47, 50, 46, 56, 42, 48, 45, 1),
    (UUID(), @munsterClub, @flyhalfId, 'Dylan Hicks', 1.30, 55, 44, 75, 72, 48, 29, 31, 1),
    (UUID(), @dragonsClub, @centreId, 'Fetuli Paea', 1.30, 46, 52, 23, 75, 48, 54, 37, 1),
    (UUID(), @zebreParmaClub, @hookerId, 'Giampietro Ribaldi', 1.30, 33, 60, 21, 74, 45, 45, 49, 1),
    (UUID(), @zebreParmaClub, @hookerId, 'Giovanni Quattrini', 1.30, 41, 57, 19, 57, 47, 43, 50, 1),
    (UUID(), @edinburghClub, @scrumhalfId, 'Hamish McArthur', 1.30, 52, 46, 64, 72, 48, 28, 37, 1),
    (UUID(), @cardiffClub, @flyhalfId, 'Harri Wilde', 1.30, 55, 44, 72, 72, 48, 29, 32, 1),
    (UUID(), @dragonsClub, @wingId, 'Harry Rees-Weldon', 1.30, 58, 44, 44, 72, 48, 29, 40, 1),
    (UUID(), @leinsterClub, @wingId, 'Hugo McLaughlin', 1.30, 53, 29, 60, 81, 48, 60, 31, 1),
    (UUID(), @stormersClub, @scrumhalfId, 'Imad Khan', 1.30, 59, 36, 50, 48, 41, 51, 46, 1),
    (UUID(), @zebreParmaClub, @looseForwardId, 'Jacopo Bianchi', 1.30, 35, 63, 21, 46, 46, 56, 47, 1),
    (UUID(), @sharksClub, @flyhalfId, 'Jean Smith', 1.30, 42, 36, 64, 69, 47, 58, 42, 1),
    (UUID(), @ulsterClub, @centreId, 'Jonny Scott', 1.30, 55, 42, 46, 78, 41, 42, 39, 1),
    (UUID(), @zebreParmaClub, @propId, 'Juan-Manuel Pitinari', 1.30, 38, 62, 25, 38, 52, 55, 46, 1),
    (UUID(), @lionsClub, @wingId, 'Keagan Smith', 1.30, 58, 44, 44, 72, 48, 31, 39, 1),
    (UUID(), @lionsClub, @scrumhalfId, 'Layton Horn', 1.30, 52, 45, 60, 73, 48, 31, 38, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'Lorcan McLoughlin', 1.30, 46, 56, 25, 65, 44, 47, 38, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Luca Ribbens', 1.30, 48, 60, 23, 64, 48, 30, 44, 1),
    (UUID(), @stormersClub, @centreId, 'Luke Burger', 1.30, 55, 50, 44, 70, 48, 31, 37, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Luke Crosbie', 1.30, 39, 59, 24, 63, 44, 51, 44, 1),
    (UUID(), @ospreysClub, @wingId, 'Luke Morgan', 1.30, 42, 55, 60, 55, 47, 55, 47, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Luke Murphy', 1.30, 48, 60, 26, 64, 48, 29, 41, 1),
    (UUID(), @ospreysClub, @flyhalfId, 'Luke Scully', 1.30, 53, 45, 73, 73, 47, 32, 32, 1),
    (UUID(), @ospreysClub, @looseForwardId, 'Math Iorwerth-Scott', 1.30, 48, 60, 24, 64, 48, 30, 44, 1),
    (UUID(), @dragonsClub, @lockId, 'Matthew Screech', 1.30, 53, 52, 16, 65, 45, 50, 50, 1),
    (UUID(), @cardiffClub, @fullbackId, 'Matty Young', 1.30, 57, 45, 54, 72, 48, 29, 34, 1),
    (UUID(), @connachtClub, @looseForwardId, 'Max Flynn', 1.30, 48, 60, 16, 64, 48, 29, 44, 1),
    (UUID(), @dragonsClub, @scrumhalfId, 'Morgan Lloyd', 1.30, 49, 48, 58, 77, 46, 32, 39, 1),
    (UUID(), @ospreysClub, @flyhalfId, 'Owen Erasmus', 1.30, 55, 44, 67, 72, 48, 28, 35, 1),
    (UUID(), @cardiffClub, @propId, 'Rhys Barratt', 1.30, 20, 57, 8, 65, 58, 66, 48, 1),
    (UUID(), @edinburghClub, @wingId, 'Ross McCann', 1.30, 45, 35, 42, 81, 49, 59, 38, 1),
    (UUID(), @lionsClub, @flyhalfId, 'Sam Francis', 1.30, 54, 43, 72, 72, 47, 31, 34, 1),
    (UUID(), @glasgowWarriorsClub, @propId, 'Sam Talakai', 1.30, 53, 55, 23, 67, 52, 41, 47, 1),
    (UUID(), @munsterClub, @centreId, 'Sean O''Brien', 1.30, 50, 36, 38, 43, 47, 59, 52, 1),
    (UUID(), @cardiffClub, @wingId, 'Theo Cabango', 1.30, 58, 44, 45, 72, 48, 32, 39, 1),
    (UUID(), @munsterClub, @flyhalfId, 'Tom Wood', 1.30, 49, 45, 66, 75, 44, 35, 36, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Wandile Mlaba', 1.30, 52, 58, 25, 66, 48, 22, 44, 1),
    (UUID(), @leinsterClub, @propId, 'Andrew Porter', 1.20, 36, 58, 15, 53, 50, 52, 48, 1),
    (UUID(), @leinsterClub, @hookerId, 'Bobby Sheehan', 1.20, 49, 59, 16, 69, 47, 33, 41, 1),
    (UUID(), @ulsterClub, @wingId, 'Bradley McNamara', 1.20, 58, 44, 46, 72, 48, 32, 36, 1),
    (UUID(), @sharksClub, @scrumhalfId, 'Ceano Everson', 1.20, 52, 46, 56, 72, 48, 28, 36, 1),
    (UUID(), @leinsterClub, @lockId, 'Conor O''Tighearnaigh', 1.20, 49, 59, 21, 70, 42, 36, 40, 1),
    (UUID(), @connachtClub, @looseForwardId, 'David Walsh', 1.20, 48, 60, 23, 64, 48, 29, 41, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Diarmuid Mangan', 1.20, 40, 59, 12, 24, 55, 59, 45, 1),
    (UUID(), @dragonsClub, @propId, 'Dillon Lewis', 1.20, 41, 64, 16, 54, 46, 51, 40, 1),
    (UUID(), @bullsClub, @propId, 'Francois Klopper', 1.20, 40, 50, 19, 49, 51, 56, 47, 1),
    (UUID(), @munsterClub, @centreId, 'Gene O''Leary-Kareem', 1.20, 55, 50, 45, 70, 48, 29, 34, 1),
    (UUID(), @ospreysClub, @looseForwardId, 'Gwilym Evans', 1.20, 38, 62, 19, 77, 46, 42, 38, 1),
    (UUID(), @bullsClub, @fullbackId, 'Henry Immelman', 1.20, 57, 45, 56, 72, 48, 29, 38, 1),
    (UUID(), @dragonsClub, @wingId, 'Huw Anderson', 1.20, 34, 31, 60, 83, 51, 58, 44, 1),
    (UUID(), @ulsterClub, @flyhalfId, 'James Humphreys', 1.20, 43, 44, 68, 78, 43, 40, 40, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Josh Ericson', 1.20, 47, 60, 23, 65, 47, 30, 42, 1),
    (UUID(), @bullsClub, @wingId, 'Katlego Letebele', 1.20, 58, 44, 43, 72, 48, 30, 36, 1),
    (UUID(), @glasgowWarriorsClub, @centreId, 'Kerr Johnston', 1.20, 51, 43, 38, 75, 45, 48, 34, 1),
    (UUID(), @edinburghClub, @centreId, 'Kienan Higgins', 1.20, 55, 50, 45, 70, 48, 32, 33, 1),
    (UUID(), @stormersClub, @flyhalfId, 'Kyle Smith', 1.20, 55, 44, 74, 72, 48, 29, 28, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Liam Molony', 1.20, 48, 60, 20, 64, 48, 30, 43, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Louw Nel', 1.20, 46, 60, 27, 67, 45, 32, 42, 1),
    (UUID(), @bullsClub, @propId, 'Mornay Smith', 1.20, 36, 60, 17, 53, 49, 52, 44, 1),
    (UUID(), @connachtClub, @propId, 'Peter Dooley', 1.20, 29, 69, 26, 64, 47, 47, 44, 1),
    (UUID(), @sharksClub, @wingId, 'Phiko Sobahle', 1.20, 47, 35, 48, 78, 45, 64, 33, 1),
    (UUID(), @dragonsClub, @propId, 'Rodrigo Martinez', 1.20, 23, 55, 26, 67, 54, 59, 46, 1),
    (UUID(), @lionsClub, @centreId, 'Rynhardt Jonker', 1.20, 40, 47, 57, 56, 47, 46, 44, 1),
    (UUID(), @stormersClub, @wingId, 'Sako Makata', 1.20, 58, 44, 47, 72, 48, 32, 34, 1),
    (UUID(), @benettonClub, @looseForwardId, 'Sebastian Negri', 1.20, 49, 61, 25, 51, 46, 51, 39, 1),
    (UUID(), @sharksClub, @propId, 'Simphiwe Matanzima', 1.20, 38, 59, 14, 49, 50, 58, 42, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Siya Kolisi', 1.20, 63, 33, 21, 78, 56, 59, 42, 1),
    (UUID(), @scarletsClub, @flyhalfId, 'Steff Jac Jones', 1.20, 55, 44, 72, 72, 48, 28, 31, 1),
    (UUID(), @bullsClub, @scrumhalfId, 'Zak Burger', 1.20, 45, 40, 48, 85, 41, 45, 44, 1),
    (UUID(), @connachtClub, @looseForwardId, 'Bobby Power', 1.10, 48, 60, 21, 64, 48, 29, 37, 1),
    (UUID(), @sharksClub, @hookerId, 'Bryce Calvert', 1.10, 45, 58, 17, 66, 48, 31, 43, 1),
    (UUID(), @ulsterClub, @propId, 'Callum Reid', 1.10, 45, 49, 26, 61, 44, 47, 44, 1),
    (UUID(), @scarletsClub, @wingId, 'Callum Woolley', 1.10, 53, 37, 49, 77, 47, 50, 28, 1),
    (UUID(), @glasgowWarriorsClub, @wingId, 'Cameron van Wyk', 1.10, 58, 44, 48, 72, 48, 22, 36, 1),
    (UUID(), @leinsterClub, @flyhalfId, 'Caspar Gabriel', 1.10, 54, 45, 72, 73, 48, 28, 28, 1),
    (UUID(), @benettonClub, @propId, 'Destiny Aminu', 1.10, 26, 63, 32, 58, 46, 49, 49, 1),
    (UUID(), @lionsClub, @lockId, 'Dylan Sjoblom', 1.10, 29, 54, 15, 78, 51, 62, 35, 1),
    (UUID(), @scarletsClub, @centreId, 'Elis Price', 1.10, 55, 50, 38, 70, 48, 28, 33, 1),
    (UUID(), @sharksClub, @centreId, 'Francois Venter', 1.10, 46, 50, 30, 85, 45, 47, 49, 1),
    (UUID(), @dragonsClub, @flyhalfId, 'Harri Ford', 1.10, 55, 44, 74, 72, 48, 29, 27, 1),
    (UUID(), @scarletsClub, @lockId, 'Harvey Cuckson', 1.10, 43, 50, 25, 33, 54, 61, 40, 1),
    (UUID(), @cardiffClub, @scrumhalfId, 'Ieuan Davies', 1.10, 52, 46, 62, 72, 48, 29, 32, 1),
    (UUID(), @stormersClub, @wingId, 'JC Mars', 1.10, 48, 34, 36, 78, 47, 65, 30, 1),
    (UUID(), @bullsClub, @lockId, 'JF van Heerden', 1.10, 29, 41, 17, 55, 61, 77, 41, 1),
    (UUID(), @ulsterClub, @lockId, 'Joe Hopes', 1.10, 23, 58, 14, 68, 55, 58, 42, 1),
    (UUID(), @bullsClub, @flyhalfId, 'Johan Goosen', 1.10, 55, 44, 78, 72, 48, 26, 42, 1),
    (UUID(), @ospreysClub, @flyhalfId, 'Keillen Cullen', 1.10, 55, 44, 69, 72, 48, 30, 29, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'Lucas de la Rua', 1.10, 48, 60, 28, 64, 48, 29, 35, 1),
    (UUID(), @ospreysClub, @scrumhalfId, 'Luke Davies', 1.10, 53, 41, 50, 63, 37, 42, 47, 1),
    (UUID(), @benettonClub, @hookerId, 'Marco Manfredi', 1.10, 45, 58, 21, 66, 48, 32, 42, 1),
    (UUID(), @stormersClub, @centreId, 'Markus Muller', 1.10, 52, 52, 45, 72, 47, 27, 32, 1),
    (UUID(), @cardiffClub, @hookerId, 'Max Pearce', 1.10, 45, 58, 23, 66, 48, 30, 44, 1),
    (UUID(), @benettonClub, @looseForwardId, 'Michele Lamaro', 1.10, 24, 45, 8, 45, 64, 84, 41, 1),
    (UUID(), @glasgowWarriorsClub, @propId, 'Nathan McBeth', 1.10, 42, 53, 21, 34, 51, 50, 50, 1),
    (UUID(), @dragonsClub, @scrumhalfId, 'Niall Armstrong', 1.10, 36, 55, 49, 87, 44, 42, 35, 1),
    (UUID(), @zebreParmaClub, @scrumhalfId, 'Nikolaj Varottoj', 1.10, 52, 46, 60, 72, 48, 27, 35, 1),
    (UUID(), @connachtClub, @looseForwardId, 'Oisin McCormack', 1.10, 49, 59, 16, 71, 45, 34, 37, 1),
    (UUID(), @leinsterClub, @scrumhalfId, 'Oliver Coffey', 1.10, 52, 46, 63, 72, 48, 29, 31, 1),
    (UUID(), @cardiffClub, @centreId, 'Osian Darwin-Lewis', 1.10, 54, 50, 40, 70, 48, 28, 32, 1),
    (UUID(), @cardiffClub, @centreId, 'Rory Jennings', 1.10, 27, 43, 49, 89, 53, 66, 30, 1),
    (UUID(), @leinsterClub, @wingId, 'Ruben Moloney', 1.10, 56, 46, 36, 66, 41, 46, 33, 1),
    (UUID(), @lionsClub, @lockId, 'Ruben Schoeman', 1.10, 28, 64, 18, 56, 47, 57, 44, 1),
    (UUID(), @stormersClub, @propId, 'Sazi Sandi', 1.10, 42, 51, 18, 47, 46, 50, 52, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Siya Dube', 1.10, 48, 60, 26, 64, 48, 30, 37, 1),
    (UUID(), @ospreysClub, @propId, 'Steff Thomas', 1.10, 29, 57, 23, 56, 50, 57, 45, 1),
    (UUID(), @stormersClub, @centreId, 'Suleiman Hartzenberg', 1.10, 38, 37, 46, 74, 49, 66, 35, 1),
    (UUID(), @glasgowWarriorsClub, @hookerId, 'Tavi Tuipulotu', 1.10, 45, 58, 18, 66, 48, 30, 43, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Tiaan Wessels', 1.10, 48, 60, 23, 64, 48, 31, 36, 1),
    (UUID(), @cardiffClub, @scrumhalfId, 'Aled Davies', 1.00, 37, 40, 66, 79, 49, 50, 48, 1),
    (UUID(), @bullsClub, @wingId, 'Aphiwe Dyantyi', 1.00, 58, 44, 38, 72, 48, 29, 39, 1),
    (UUID(), @munsterClub, @scrumhalfId, 'Ben O''Donovan', 1.00, 55, 41, 52, 76, 42, 30, 38, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Ben Williams', 1.00, 45, 59, 26, 67, 46, 33, 38, 1),
    (UUID(), @connachtClub, @scrumhalfId, 'Colm Reilly', 1.00, 35, 49, 62, 84, 40, 44, 37, 1),
    (UUID(), @leinsterClub, @centreId, 'Connor Fahy', 1.00, 55, 50, 41, 70, 48, 29, 30, 1);

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (UUID(), @munsterClub, @lockId, 'Conor Ryan', 1.00, 42, 58, 22, 66, 48, 30, 43, 1),
    (UUID(), @sharksClub, @lockId, 'Corne Rahl', 1.00, 34, 58, 21, 63, 44, 46, 48, 1),
    (UUID(), @sharksClub, @lockId, 'Deon Slabbert', 1.00, 29, 48, 18, 82, 53, 71, 27, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Divan Fuller', 1.00, 48, 60, 16, 64, 48, 29, 36, 1),
    (UUID(), @sharksClub, @lockId, 'Eben Etzebeth', 1.00, 37, 53, 23, 70, 49, 69, 38, 1),
    (UUID(), @ospreysClub, @hookerId, 'Efan Daniel', 1.00, 42, 64, 25, 65, 41, 34, 39, 1),
    (UUID(), @cardiffClub, @centreId, 'Elijah Evans', 1.00, 52, 52, 39, 73, 45, 30, 32, 1),
    (UUID(), @zebreParmaClub, @centreId, 'Enrico Lucchin', 1.00, 40, 61, 33, 53, 46, 54, 35, 1),
    (UUID(), @benettonClub, @propId, 'Giosue Zilocchi', 1.00, 29, 56, 15, 60, 51, 53, 46, 1),
    (UUID(), @scarletsClub, @fullbackId, 'Ioan Nicholas', 1.00, 46, 31, 52, 83, 42, 55, 39, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Izan Esterhuizen', 1.00, 48, 60, 19, 64, 48, 32, 35, 1),
    (UUID(), @bullsClub, @lockId, 'Jaco Grobbelaar', 1.00, 40, 49, 15, 73, 46, 54, 39, 1),
    (UUID(), @glasgowWarriorsClub, @propId, 'Jamie Bhatti', 1.00, 43, 62, 18, 39, 51, 51, 42, 1),
    (UUID(), @stormersClub, @flyhalfId, 'Jean-Luc du Plessis', 1.00, 55, 44, 69, 72, 48, 27, 36, 1),
    (UUID(), @leinsterClub, @hookerId, 'John McKee', 1.00, 41, 54, 26, 47, 46, 48, 45, 1),
    (UUID(), @munsterClub, @propId, 'Josh Wycherley', 1.00, 39, 62, 21, 57, 37, 40, 50, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Keke Morabe', 1.00, 48, 53, 24, 73, 40, 34, 41, 1),
    (UUID(), @ospreysClub, @hookerId, 'Lewis Lloyd', 1.00, 56, 55, 24, 65, 34, 38, 39, 1),
    (UUID(), @lionsClub, @centreId, 'Manuel Rass', 1.00, 54, 50, 41, 71, 47, 32, 29, 1),
    (UUID(), @lionsClub, @hookerId, 'Marno Grobbelaar', 1.00, 45, 58, 24, 66, 48, 30, 38, 1),
    (UUID(), @edinburghClub, @fullbackId, 'Matt Davidson', 1.00, 53, 40, 58, 75, 46, 33, 33, 1),
    (UUID(), @zebreParmaClub, @propId, 'Matteo Nocera', 1.00, 41, 52, 20, 35, 47, 58, 48, 1),
    (UUID(), @stormersClub, @wingId, 'Mfundo Ndhlovu', 1.00, 58, 44, 40, 72, 48, 31, 31, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Michael Foy', 1.00, 48, 60, 25, 64, 48, 28, 33, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Nama Xaba', 1.00, 31, 67, 23, 69, 44, 42, 40, 1),
    (UUID(), @munsterClub, @looseForwardId, 'Oisin Minogue', 1.00, 48, 60, 16, 64, 48, 28, 38, 1),
    (UUID(), @munsterClub, @scrumhalfId, 'Paddy Patterson', 1.00, 33, 56, 44, 74, 42, 48, 36, 1),
    (UUID(), @leinsterClub, @wingId, 'Paidi Farrell', 1.00, 58, 44, 42, 72, 48, 29, 30, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Ruan Delport', 1.00, 28, 54, 23, 83, 48, 47, 42, 1),
    (UUID(), @dragonsClub, @hookerId, 'Sam Scarfe', 1.00, 43, 59, 20, 68, 46, 30, 43, 1),
    (UUID(), @ulsterClub, @centreId, 'Stewart Moore', 1.00, 53, 51, 43, 72, 47, 33, 29, 1),
    (UUID(), @cardiffClub, @lockId, 'Teddy Williams', 1.00, 32, 62, 16, 53, 49, 66, 31, 1),
    (UUID(), @munsterClub, @wingId, 'Thaakir Abrahams', 1.00, 42, 28, 23, 55, 59, 80, 37, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Thomas Dyer', 1.00, 37, 60, 19, 76, 42, 41, 40, 1),
    (UUID(), @sharksClub, @scrumhalfId, 'Tiaan Fourie', 1.00, 52, 46, 55, 72, 48, 32, 29, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Todd Lawlor', 1.00, 50, 50, 30, 70, 48, 28, 39, 1),
    (UUID(), @bullsClub, @propId, 'Wilco Louw', 1.00, 34, 45, 26, 71, 59, 58, 40, 1),
    (UUID(), @dragonsClub, @hookerId, 'Wills Austin', 1.00, 44, 58, 19, 69, 44, 39, 36, 1),
    (UUID(), @scarletsClub, @propId, 'Alec Hepburn', 0.90, 36, 51, 20, 63, 51, 52, 48, 1),
    (UUID(), @leinsterClub, @propId, 'Alex Usanov', 0.90, 37, 57, 23, 70, 45, 46, 35, 1),
    (UUID(), @leinsterClub, @wingId, 'Andrew Osborne', 0.90, 56, 33, 60, 35, 43, 50, 44, 1),
    (UUID(), @scarletsClub, @propId, 'Archer Holz', 0.90, 16, 46, 22, 71, 61, 65, 43, 1),
    (UUID(), @lionsClub, @lockId, 'Batho Hlekani', 0.90, 42, 58, 26, 66, 48, 29, 37, 1),
    (UUID(), @benettonClub, @hookerId, 'Bautista Bernasconi', 0.90, 38, 53, 19, 35, 46, 60, 46, 1),
    (UUID(), @ospreysClub, @lockId, 'Ben Roberts', 0.90, 42, 59, 17, 67, 47, 32, 40, 1),
    (UUID(), @ospreysClub, @propId, 'Cameron Jones', 0.90, 60, 51, 19, 51, 41, 35, 39, 1),
    (UUID(), @bullsClub, @centreId, 'Chris Smit', 0.90, 55, 50, 39, 70, 48, 30, 29, 1),
    (UUID(), @edinburghClub, @scrumhalfId, 'Conor McAlpine', 0.90, 48, 40, 61, 76, 43, 31, 40, 1),
    (UUID(), @lionsClub, @propId, 'Conrad van Vuuren', 0.90, 58, 53, 21, 36, 37, 34, 51, 1),
    (UUID(), @scarletsClub, @scrumhalfId, 'Dane Blacker', 0.90, 38, 31, 58, 68, 49, 56, 42, 1),
    (UUID(), @connachtClub, @wingId, 'Daniel Ryan', 0.90, 48, 38, 36, 63, 45, 59, 32, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'Evan Rees', 0.90, 50, 50, 39, 70, 48, 28, 32, 1),
    (UUID(), @dragonsClub, @wingId, 'Ewan Rosser', 0.90, 43, 44, 45, 80, 48, 51, 26, 1),
    (UUID(), @zebreParmaClub, @lockId, 'Francesco Ruffolo', 0.90, 42, 58, 17, 66, 48, 31, 42, 1),
    (UUID(), @stormersClub, @lockId, 'Gary Porter', 0.90, 42, 58, 23, 66, 48, 30, 42, 1),
    (UUID(), @zebreParmaClub, @scrumhalfId, 'Gonzalo Garcia', 0.90, 47, 48, 55, 40, 42, 58, 31, 1),
    (UUID(), @glasgowWarriorsClub, @hookerId, 'Grant Stewart', 0.90, 44, 57, 28, 67, 47, 29, 43, 1),
    (UUID(), @lionsClub, @scrumhalfId, 'Hasseim Pead', 0.90, 59, 32, 51, 84, 39, 34, 38, 1),
    (UUID(), @zebreParmaClub, @propId, 'Ion Neculai', 0.90, 49, 57, 18, 54, 38, 38, 43, 1),
    (UUID(), @scarletsClub, @wingId, 'Iori Badham', 0.90, 42, 46, 45, 60, 46, 46, 36, 1),
    (UUID(), @scarletsClub, @lockId, 'Jake Ball', 0.90, 33, 59, 17, 35, 57, 56, 53, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'James McNabney', 0.90, 48, 60, 22, 64, 48, 31, 31, 1),
    (UUID(), @cardiffClub, @propId, 'Joe Cowell', 0.90, 40, 59, 24, 64, 47, 32, 40, 1),
    (UUID(), @connachtClub, @centreId, 'John Devine', 0.90, 45, 39, 24, 47, 51, 68, 35, 1),
    (UUID(), @sharksClub, @centreId, 'Lukhanyo Am', 0.90, 47, 40, 67, 49, 51, 56, 33, 1),
    (UUID(), @sharksClub, @wingId, 'Makazole Mapimpi', 0.90, 45, 34, 16, 45, 65, 70, 49, 1),
    (UUID(), @ospreysClub, @looseForwardId, 'Marco de Witt', 0.90, 49, 59, 17, 67, 45, 33, 31, 1),
    (UUID(), @munsterClub, @propId, 'Mark Donnelly', 0.90, 32, 60, 23, 55, 46, 65, 30, 1),
    (UUID(), @sharksClub, @wingId, 'Marnus Potgieter', 0.90, 52, 42, 45, 76, 46, 45, 26, 1),
    (UUID(), @sharksClub, @lockId, 'Meno Barnard', 0.90, 42, 58, 21, 66, 48, 30, 42, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Osian Williams', 0.90, 48, 60, 27, 64, 48, 27, 31, 1),
    (UUID(), @leinsterClub, @propId, 'Paddy McCarthy', 0.90, 36, 62, 16, 75, 46, 43, 33, 1),
    (UUID(), @lionsClub, @wingId, 'Rabz Maxwane', 0.90, 58, 44, 41, 72, 48, 30, 30, 1),
    (UUID(), @lionsClub, @lockId, 'Raynard Roets', 0.90, 42, 58, 16, 66, 48, 32, 40, 1),
    (UUID(), @stormersClub, @lockId, 'Riley Norton', 0.90, 42, 58, 26, 66, 48, 28, 39, 1),
    (UUID(), @glasgowWarriorsClub, @propId, 'Rory Sutherland', 0.90, 53, 52, 15, 50, 41, 39, 54, 1),
    (UUID(), @ospreysClub, @scrumhalfId, 'Scott Whitlock', 0.90, 52, 46, 63, 72, 48, 29, 26, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Tiaan Sparrow', 0.90, 48, 60, 27, 64, 48, 28, 30, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Tino Mavesere', 0.90, 45, 57, 28, 41, 41, 46, 41, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Tom Dodd', 0.90, 40, 54, 26, 78, 39, 44, 37, 1),
    (UUID(), @ospreysClub, @centreId, 'Tom Florence', 0.90, 48, 49, 40, 64, 42, 45, 31, 1),
    (UUID(), @leinsterClub, @looseForwardId, 'Will Connors', 0.90, 43, 63, 19, 30, 42, 46, 45, 1),
    (UUID(), @zebreParmaClub, @fullbackId, 'Albert Batista', 0.80, 37, 38, 25, 64, 50, 72, 31, 1),
    (UUID(), @leinsterClub, @lockId, 'Billy Corrigan', 0.80, 42, 58, 24, 66, 48, 29, 37, 1),
    (UUID(), @dragonsClub, @propId, 'Cebo Dlamini', 0.80, 40, 58, 20, 62, 48, 32, 39, 1),
    (UUID(), @sharksClub, @lockId, 'Coetzee le Roux', 0.80, 42, 58, 17, 66, 48, 31, 39, 1),
    (UUID(), @munsterClub, @hookerId, 'Danny Sheahan', 0.80, 45, 58, 19, 66, 48, 30, 36, 1),
    (UUID(), @munsterClub, @propId, 'Darragh McSweeney', 0.80, 40, 58, 23, 62, 48, 31, 39, 1),
    (UUID(), @bullsClub, @fullbackId, 'Devon Williams', 0.80, 47, 35, 58, 52, 52, 54, 41, 1),
    (UUID(), @munsterClub, @propId, 'Emmett Calvey', 0.80, 40, 58, 24, 62, 48, 29, 39, 1),
    (UUID(), @bullsClub, @propId, 'Etienne Janeke', 0.80, 39, 58, 20, 63, 47, 32, 41, 1),
    (UUID(), @edinburghClub, @lockId, 'Euan McVie', 0.80, 42, 57, 19, 68, 47, 29, 39, 1),
    (UUID(), @glasgowWarriorsClub, @propId, 'Fin Richardson', 0.80, 29, 51, 21, 81, 48, 59, 31, 1),
    (UUID(), @benettonClub, @flyhalfId, 'Giuliano Avaca', 0.80, 53, 42, 72, 69, 46, 31, 26, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Hacjivah Dayimani', 0.80, 46, 55, 26, 50, 41, 35, 46, 1),
    (UUID(), @benettonClub, @propId, 'Ivan Nemer', 0.80, 30, 62, 43, 70, 43, 36, 39, 1),
    (UUID(), @munsterClub, @scrumhalfId, 'Jake O''Riordan', 0.80, 48, 46, 60, 74, 47, 29, 28, 1),
    (UUID(), @lionsClub, @hookerId, 'Janco Uys', 0.80, 45, 58, 20, 66, 48, 32, 31, 1),
    (UUID(), @edinburghClub, @hookerId, 'Jerry Blyth-Lafferty', 0.80, 40, 62, 22, 55, 37, 33, 46, 1),
    (UUID(), @dragonsClub, @centreId, 'Joe Westwood', 0.80, 45, 52, 45, 60, 42, 33, 38, 1),
    (UUID(), @benettonClub, @looseForwardId, 'John Bryant', 0.80, 47, 59, 22, 63, 38, 40, 34, 1),
    (UUID(), @connachtClub, @propId, 'Jordan Duggan', 0.80, 30, 54, 23, 72, 49, 50, 35, 1),
    (UUID(), @ospreysClub, @lockId, 'Lewis Jones', 0.80, 39, 60, 19, 68, 47, 33, 37, 1),
    (UUID(), @dragonsClub, @propId, 'Luke Yendle', 0.80, 40, 58, 16, 62, 48, 32, 41, 1),
    (UUID(), @stormersClub, @hookerId, 'Lukhanyo Vokozela', 0.80, 42, 58, 20, 71, 44, 33, 36, 1),
    (UUID(), @connachtClub, @hookerId, 'Matthew Victory', 0.80, 35, 63, 27, 66, 41, 35, 41, 1),
    (UUID(), @dragonsClub, @lockId, 'Nick Thomas', 0.80, 42, 58, 21, 66, 48, 29, 37, 1),
    (UUID(), @bullsClub, @lockId, 'Reinhardt Ludwig', 0.80, 28, 58, 24, 54, 54, 55, 36, 1),
    (UUID(), @zebreParmaClub, @propId, 'Riccardo Genovese', 0.80, 40, 58, 14, 62, 48, 32, 42, 1),
    (UUID(), @benettonClub, @hookerId, 'Richie Asiata', 0.80, 43, 56, 20, 70, 45, 34, 40, 1),
    (UUID(), @edinburghClub, @lockId, 'Rob Carmichael', 0.80, 42, 58, 16, 66, 48, 31, 39, 1),
    (UUID(), @ospreysClub, @wingId, 'Ryan Conbeer', 0.80, 43, 38, 53, 80, 41, 54, 30, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'Sean Reffell', 0.80, 43, 64, 17, 56, 42, 34, 37, 1),
    (UUID(), @bullsClub, @wingId, 'Sergeal Petersen', 0.80, 66, 25, 46, 68, 44, 50, 30, 1),
    (UUID(), @lionsClub, @looseForwardId, 'Siba Qoma', 0.80, 52, 50, 26, 57, 37, 34, 45, 1),
    (UUID(), @leinsterClub, @hookerId, 'Stephen Smyth', 0.80, 45, 58, 22, 66, 48, 29, 32, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'Tom Brigg', 0.80, 42, 58, 28, 55, 46, 45, 30, 1),
    (UUID(), @scarletsClub, @wingId, 'Tomi Lewis', 0.80, 49, 36, 45, 56, 43, 61, 33, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Tristan Davies', 0.80, 41, 57, 20, 61, 44, 34, 42, 1),
    (UUID(), @ospreysClub, @lockId, 'Will Greatbanks', 0.80, 40, 58, 17, 68, 47, 31, 41, 1),
    (UUID(), @leinsterClub, @lockId, 'Alan Spicer', 0.70, 40, 58, 23, 61, 47, 38, 30, 1),
    (UUID(), @scarletsClub, @lockId, 'Alex Groves', 0.70, 40, 56, 19, 60, 45, 40, 37, 1),
    (UUID(), @leinsterClub, @propId, 'Alex Mullan', 0.70, 40, 58, 20, 62, 48, 29, 36, 1),
    (UUID(), @ulsterClub, @centreId, 'Ben Carson', 0.70, 50, 41, 48, 57, 40, 47, 33, 1),
    (UUID(), @cardiffClub, @looseForwardId, 'Ben Donnell', 0.70, 35, 57, 20, 70, 44, 46, 33, 1),
    (UUID(), @sharksClub, @propId, 'Cameron Dawson', 0.70, 33, 59, 25, 69, 45, 34, 40, 1),
    (UUID(), @sharksClub, @wingId, 'Christie Grobbelaar', 0.70, 44, 34, 37, 55, 47, 75, 24, 1),
    (UUID(), @scarletsClub, @looseForwardId, 'Dan Davis', 0.70, 29, 53, 16, 54, 48, 52, 45, 1),
    (UUID(), @sharksClub, @propId, 'Dian Bleuler', 0.70, 38, 56, 21, 60, 44, 33, 42, 1),
    (UUID(), @sharksClub, @hookerId, 'Eddie Swart', 0.70, 30, 46, 18, 73, 48, 56, 39, 1),
    (UUID(), @cardiffClub, @scrumhalfId, 'Ellis Bevan', 0.70, 50, 46, 60, 57, 42, 36, 30, 1),
    (UUID(), @sharksClub, @hookerId, 'Ethan Bester', 0.70, 44, 56, 18, 64, 47, 29, 36, 1),
    (UUID(), @ospreysClub, @hookerId, 'Ethan Lewis', 0.70, 46, 60, 22, 72, 44, 28, 35, 1),
    (UUID(), @lionsClub, @flyhalfId, 'Gianni Lombard', 0.70, 55, 38, 60, 79, 40, 34, 27, 1),
    (UUID(), @edinburghClub, @looseForwardId, 'Hamish Watson', 0.70, 45, 60, 18, 68, 47, 28, 45, 1),
    (UUID(), @dragonsClub, @centreId, 'Harri Ackerman', 0.70, 40, 57, 40, 53, 45, 38, 33, 1),
    (UUID(), @scarletsClub, @hookerId, 'Isaac Young', 0.70, 45, 58, 21, 66, 48, 30, 31, 1),
    (UUID(), @sharksClub, @lockId, 'JJ Scheepers', 0.70, 42, 58, 25, 66, 48, 32, 29, 1),
    (UUID(), @dragonsClub, @hookerId, 'James Benjamin', 0.70, 41, 59, 20, 70, 47, 32, 39, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Jannes Kirsten', 0.70, 48, 60, 21, 64, 48, 27, 35, 1),
    (UUID(), @leinsterClub, @hookerId, 'Lee Fitzpatrick', 0.70, 45, 58, 21, 66, 48, 27, 31, 1),
    (UUID(), @lionsClub, @propId, 'Leon Lyons', 0.70, 40, 58, 17, 62, 48, 32, 39, 1),
    (UUID(), @sharksClub, @scrumhalfId, 'Luan Giliomee', 0.70, 55, 34, 50, 61, 43, 44, 32, 1),
    (UUID(), @leinsterClub, @lockId, 'Mahon Ronan', 0.70, 42, 58, 20, 66, 48, 28, 35, 1),
    (UUID(), @benettonClub, @centreId, 'Malakai Fekitoa', 0.70, 41, 36, 55, 26, 57, 66, 45, 1),
    (UUID(), @munsterClub, @hookerId, 'Max Clein', 0.70, 45, 58, 18, 66, 48, 30, 30, 1),
    (UUID(), @munsterClub, @propId, 'Michael Ala''alatoa', 0.70, 28, 54, 19, 43, 59, 64, 44, 1),
    (UUID(), @connachtClub, @hookerId, 'Mikey Yarr', 0.70, 43, 58, 18, 64, 46, 29, 37, 1),
    (UUID(), @lionsClub, @propId, 'Morgan Naude', 0.70, 40, 58, 15, 62, 48, 32, 38, 1),
    (UUID(), @benettonClub, @looseForwardId, 'Nelson Casartelli', 0.70, 45, 55, 28, 61, 44, 29, 38, 1);

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (UUID(), @edinburghClub, @propId, 'Rhys Litterick', 0.70, 40, 58, 23, 62, 48, 32, 37, 1),
    (UUID(), @ulsterClub, @hookerId, 'Rob Herring', 0.70, 46, 55, 12, 41, 54, 50, 46, 1),
    (UUID(), @ulsterClub, @propId, 'Rory McGuire', 0.70, 40, 58, 21, 62, 48, 31, 36, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Ruan Ackermann', 0.70, 39, 64, 18, 59, 44, 35, 37, 1),
    (UUID(), @scarletsClub, @propId, 'Sam O''Connor', 0.70, 43, 57, 21, 51, 40, 36, 46, 1),
    (UUID(), @dragonsClub, @looseForwardId, 'Solomone Funaki', 0.70, 48, 60, 16, 64, 48, 27, 36, 1),
    (UUID(), @leinsterClub, @propId, 'Tadhg Furlong', 0.70, 53, 44, 22, 64, 44, 46, 39, 1),
    (UUID(), @cardiffClub, @lockId, 'Tom Cottle', 0.70, 42, 58, 19, 66, 48, 27, 34, 1),
    (UUID(), @benettonClub, @hookerId, 'Tomas Montilla', 0.70, 43, 57, 26, 68, 45, 33, 33, 1),
    (UUID(), @sharksClub, @propId, 'Vincent Koch', 0.70, 40, 52, 15, 57, 55, 47, 49, 1),
    (UUID(), @scarletsClub, @lockId, 'Will Evans', 0.70, 42, 58, 20, 66, 48, 28, 33, 1),
    (UUID(), @bullsClub, @propId, 'Alulutho Tshakweni', 0.60, 27, 58, 17, 63, 46, 50, 34, 1),
    (UUID(), @dragonsClub, @lockId, 'Barny Langton-Cryer', 0.60, 40, 60, 20, 60, 44, 34, 32, 1),
    (UUID(), @munsterClub, @lockId, 'Conor Kennelly', 0.60, 42, 58, 20, 66, 48, 28, 30, 1),
    (UUID(), @cardiffClub, @propId, 'Corey Domachowski', 0.60, 41, 55, 23, 55, 43, 37, 38, 1),
    (UUID(), @benettonClub, @scrumhalfId, 'Cristiano Tizzano', 0.60, 50, 44, 54, 76, 42, 31, 26, 1),
    (UUID(), @connachtClub, @propId, 'Denis Buckley', 0.60, 28, 67, 24, 48, 52, 45, 43, 1),
    (UUID(), @glasgowWarriorsClub, @flyhalfId, 'Duncan Weir', 0.60, 56, 44, 72, 75, 45, 26, 28, 1),
    (UUID(), @lionsClub, @propId, 'Eddie Davids', 0.60, 23, 63, 21, 59, 44, 40, 45, 1),
    (UUID(), @ospreysClub, @lockId, 'Evan Hill', 0.60, 42, 58, 16, 66, 48, 29, 31, 1),
    (UUID(), @munsterClub, @propId, 'George Hadden', 0.60, 40, 58, 26, 62, 48, 31, 30, 1),
    (UUID(), @sharksClub, @lockId, 'Gideon Koegelenberg', 0.60, 42, 58, 23, 66, 48, 29, 32, 1),
    (UUID(), @lionsClub, @propId, 'Heiko Pohlmann', 0.60, 40, 56, 18, 61, 47, 32, 36, 1),
    (UUID(), @cardiffClub, @wingId, 'Iwan Stephens', 0.60, 47, 37, 40, 80, 42, 50, 27, 1),
    (UUID(), @bullsClub, @propId, 'JD Erasmus', 0.60, 40, 58, 16, 62, 48, 29, 34, 1),
    (UUID(), @lionsClub, @lockId, 'JR Stopforth', 0.60, 42, 58, 17, 66, 48, 32, 30, 1),
    (UUID(), @connachtClub, @flyhalfId, 'Jack Carty', 0.60, 37, 50, 77, 46, 42, 45, 35, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Jannes Potgieter', 0.60, 45, 57, 20, 58, 44, 33, 32, 1),
    (UUID(), @munsterClub, @propId, 'Kieran Ryan', 0.60, 40, 58, 14, 62, 48, 32, 36, 1),
    (UUID(), @cardiffClub, @fullbackId, 'Leigh Halfpenny', 0.60, 53, 46, 57, 74, 47, 22, 38, 1),
    (UUID(), @zebreParmaClub, @centreId, 'Luca Morisi', 0.60, 38, 57, 45, 84, 37, 37, 39, 1),
    (UUID(), @sharksClub, @looseForwardId, 'Matt Romao', 0.60, 33, 56, 23, 66, 39, 36, 43, 1),
    (UUID(), @benettonClub, @lockId, 'Mattia Midena', 0.60, 35, 55, 25, 65, 45, 33, 41, 1),
    (UUID(), @zebreParmaClub, @scrumhalfId, 'Migael Prinsloo', 0.60, 46, 41, 56, 80, 39, 31, 33, 1),
    (UUID(), @bullsClub, @looseForwardId, 'Nizaam Carr', 0.60, 49, 49, 37, 71, 40, 39, 39, 1),
    (UUID(), @connachtClub, @lockId, 'Oisin Dowling', 0.60, 42, 58, 15, 66, 48, 31, 31, 1),
    (UUID(), @munsterClub, @propId, 'Oli Jager', 0.60, 27, 58, 20, 70, 49, 47, 36, 1),
    (UUID(), @stormersClub, @propId, 'Oliver Reid', 0.60, 38, 58, 21, 64, 47, 22, 40, 1),
    (UUID(), @munsterClub, @propId, 'Ronan Foxe', 0.60, 42, 59, 21, 61, 45, 33, 33, 1),
    (UUID(), @stormersClub, @lockId, 'Salmaan Moerat', 0.60, 40, 64, 19, 32, 43, 43, 39, 1),
    (UUID(), @ulsterClub, @propId, 'Sam Crean', 0.60, 29, 56, 15, 41, 48, 54, 42, 1),
    (UUID(), @edinburghClub, @lockId, 'Sam Skinner', 0.60, 27, 50, 19, 72, 50, 61, 35, 1),
    (UUID(), @lionsClub, @propId, 'Sivu Mabece', 0.60, 40, 58, 21, 62, 48, 31, 35, 1),
    (UUID(), @scarletsClub, @lockId, 'Steve Cummins', 0.60, 45, 54, 14, 58, 48, 45, 35, 1),
    (UUID(), @lionsClub, @propId, 'Stian de Bruyn', 0.60, 40, 58, 25, 62, 48, 30, 34, 1),
    (UUID(), @zebreParmaClub, @scrumhalfId, 'Thomas Dominguez', 0.60, 43, 48, 60, 28, 38, 48, 40, 1),
    (UUID(), @cardiffClub, @propId, 'Will Davies-King', 0.60, 40, 58, 14, 62, 48, 32, 36, 1),
    (UUID(), @cardiffClub, @propId, 'Cameron Tyler-Grocott', 0.50, 40, 58, 25, 62, 48, 29, 28, 1),
    (UUID(), @stormersClub, @centreId, 'Clinton Swart', 0.50, 51, 47, 48, 74, 43, 27, 28, 1),
    (UUID(), @ulsterClub, @scrumhalfId, 'Conor McKee', 0.50, 32, 45, 51, 63, 44, 48, 34, 1),
    (UUID(), @leinsterClub, @scrumhalfId, 'Cormac Foley', 0.50, 48, 38, 53, 63, 45, 39, 29, 1),
    (UUID(), @stormersClub, @propId, 'Corne Weilbach', 0.50, 40, 58, 18, 62, 48, 32, 28, 1),
    (UUID(), @edinburghClub, @propId, 'D''arcy Rae', 0.50, 36, 49, 22, 53, 51, 51, 32, 1),
    (UUID(), @scarletsClub, @lockId, 'Dan Gemine', 0.50, 39, 54, 19, 63, 45, 28, 39, 1),
    (UUID(), @leinsterClub, @propId, 'Ed Byrne', 0.50, 43, 46, 21, 61, 44, 43, 38, 1),
    (UUID(), @dragonsClub, @hookerId, 'Elliot Dee', 0.50, 38, 55, 20, 48, 47, 39, 44, 1),
    (UUID(), @lionsClub, @hookerId, 'Franco Marais', 0.50, 33, 56, 22, 22, 49, 47, 52, 1),
    (UUID(), @zebreParmaClub, @lockId, 'Giacomo Milano', 0.50, 38, 59, 18, 70, 44, 30, 33, 1),
    (UUID(), @scarletsClub, @propId, 'Henry Thomas', 0.50, 23, 53, 9, 44, 56, 55, 52, 1),
    (UUID(), @cardiffClub, @propId, 'Ioan Emanuel', 0.50, 38, 58, 24, 64, 47, 29, 32, 1),
    (UUID(), @scarletsClub, @lockId, 'Jac Price', 0.50, 25, 62, 15, 64, 42, 48, 38, 1),
    (UUID(), @edinburghClub, @fullbackId, 'Jack Brown', 0.50, 42, 32, 37, 66, 47, 54, 31, 1),
    (UUID(), @ospreysClub, @lockId, 'James Fender', 0.50, 21, 50, 21, 46, 50, 60, 43, 1),
    (UUID(), @sharksClub, @propId, 'Lee-Marvin Mazibuko', 0.50, 37, 57, 14, 52, 45, 38, 39, 1),
    (UUID(), @ospreysClub, @lockId, 'Liam Edwards', 0.50, 42, 58, 18, 66, 48, 30, 27, 1),
    (UUID(), @zebreParmaClub, @propId, 'Luca Rizzoli', 0.50, 40, 58, 15, 62, 48, 32, 30, 1),
    (UUID(), @sharksClub, @propId, 'Mawande Mdanda', 0.50, 39, 57, 17, 52, 44, 33, 39, 1),
    (UUID(), @edinburghClub, @hookerId, 'Paddy Harrison', 0.50, 44, 56, 17, 60, 42, 33, 31, 1),
    (UUID(), @leinsterClub, @propId, 'Rabah Slimani', 0.50, 27, 64, 14, 41, 53, 42, 50, 1),
    (UUID(), @munsterClub, @propId, 'Roman Salanoa', 0.50, 40, 58, 22, 62, 48, 32, 28, 1),
    (UUID(), @lionsClub, @propId, 'Sebastian Lombard', 0.50, 32, 59, 16, 70, 45, 38, 32, 1),
    (UUID(), @zebreParmaClub, @hookerId, 'Shilo Klein', 0.50, 41, 57, 17, 56, 42, 34, 37, 1),
    (UUID(), @bullsClub, @lockId, 'Sintu Manjezi', 0.50, 39, 59, 21, 68, 47, 29, 35, 1),
    (UUID(), @connachtClub, @propId, 'Temi Lasisi', 0.50, 40, 58, 14, 62, 48, 32, 30, 1),
    (UUID(), @munsterClub, @flyhalfId, 'Tony Butler', 0.50, 42, 49, 51, 54, 38, 41, 33, 1),
    (UUID(), @dragonsClub, @propId, 'Wyn Jones', 0.50, 21, 62, 16, 46, 52, 49, 47, 1),
    (UUID(), @ulsterClub, @wingId, 'Ben Moxham', 0.40, 46, 32, 44, 60, 46, 55, 23, 1),
    (UUID(), @dragonsClub, @propId, 'Christian Coleman', 0.40, 40, 42, 18, 23, 46, 56, 45, 1),
    (UUID(), @stormersClub, @lockId, 'Connor Evans', 0.40, 33, 65, 25, 31, 41, 45, 36, 1),
    (UUID(), @munsterClub, @propId, 'Conor Bartley', 0.40, 34, 64, 16, 64, 42, 31, 33, 1),
    (UUID(), @zebreParmaClub, @looseForwardId, 'Davide Ruggeri', 0.40, 30, 48, 27, 37, 50, 60, 35, 1),
    (UUID(), @stormersClub, @looseForwardId, 'Deon Fourie', 0.40, 43, 64, 25, 48, 42, 22, 48, 1),
    (UUID(), @ulsterClub, @propId, 'Eric O''Sullivan', 0.40, 43, 57, 14, 42, 39, 40, 38, 1),
    (UUID(), @sharksClub, @hookerId, 'Fez Mbatha', 0.40, 26, 43, 11, 63, 54, 59, 36, 1),
    (UUID(), @edinburghClub, @wingId, 'Fin Doyle', 0.40, 58, 44, 41, 72, 48, 16, 32, 1),
    (UUID(), @scarletsClub, @propId, 'Gabe Hawley', 0.40, 36, 57, 18, 58, 47, 36, 31, 1),
    (UUID(), @ospreysClub, @propId, 'Gareth Thomas', 0.40, 26, 51, 14, 59, 49, 59, 34, 1),
    (UUID(), @scarletsClub, @hookerId, 'Harry Thomas', 0.40, 31, 59, 23, 66, 40, 34, 37, 1),
    (UUID(), @stormersClub, @propId, 'Hencus van Wyk', 0.40, 38, 59, 25, 60, 47, 25, 40, 1),
    (UUID(), @lionsClub, @hookerId, 'Jaco Visagie', 0.40, 45, 58, 24, 66, 48, 25, 32, 1),
    (UUID(), @edinburghClub, @propId, 'James Whitcombe', 0.40, 37, 54, 16, 58, 43, 35, 39, 1),
    (UUID(), @edinburghClub, @propId, 'Jamie Stewart', 0.40, 40, 58, 23, 62, 48, 28, 27, 1),
    (UUID(), @ulsterClub, @hookerId, 'John Andrew', 0.40, 45, 57, 17, 70, 45, 27, 32, 1),
    (UUID(), @munsterClub, @propId, 'John Ryan', 0.40, 28, 55, 18, 57, 51, 41, 49, 1),
    (UUID(), @scarletsClub, @propId, 'Josh Morse', 0.40, 41, 53, 26, 55, 35, 34, 37, 1),
    (UUID(), @lionsClub, @propId, 'Juan Schoeman', 0.40, 40, 58, 21, 62, 48, 25, 37, 1),
    (UUID(), @bullsClub, @propId, 'Juann Else', 0.40, 40, 54, 24, 62, 40, 33, 33, 1),
    (UUID(), @scarletsClub, @hookerId, 'Kirby Myhill', 0.40, 41, 57, 25, 70, 46, 26, 33, 1),
    (UUID(), @ulsterClub, @looseForwardId, 'Marcus Rea', 0.40, 35, 56, 16, 40, 39, 41, 45, 1),
    (UUID(), @edinburghClub, @propId, 'Mikey Jones', 0.40, 37, 59, 16, 58, 47, 33, 31, 1),
    (UUID(), @glasgowWarriorsClub, @propId, 'Murphy Walker', 0.40, 31, 54, 25, 49, 44, 52, 31, 1),
    (UUID(), @munsterClub, @hookerId, 'Niall Scannell', 0.40, 40, 52, 16, 54, 41, 39, 44, 1),
    (UUID(), @zebreParmaClub, @propId, 'Paolo Buonfiglio', 0.40, 15, 58, 19, 64, 52, 48, 41, 1),
    (UUID(), @bullsClub, @propId, 'Ruan Swart', 0.40, 40, 58, 14, 62, 48, 29, 27, 1),
    (UUID(), @glasgowWarriorsClub, @lockId, 'Ryan Burke', 0.40, 33, 53, 20, 74, 44, 42, 27, 1),
    (UUID(), @cardiffClub, @propId, 'Sam Wainwright', 0.40, 37, 55, 18, 50, 40, 35, 39, 1),
    (UUID(), @sharksClub, @propId, 'Simphiwe Ngobese', 0.40, 40, 58, 14, 62, 48, 28, 26, 1),
    (UUID(), @stormersClub, @scrumhalfId, 'Stefan Ungerer', 0.40, 35, 29, 49, 78, 47, 51, 38, 1),
    (UUID(), @sharksClub, @propId, 'Trevor Nyakane', 0.40, 40, 58, 23, 62, 48, 21, 42, 1),
    (UUID(), @leinsterClub, @propId, 'Andrew Sparrow', 0.30, 35, 53, 23, 57, 43, 37, 29, 1),
    (UUID(), @edinburghClub, @propId, 'Angus Williams', 0.30, 35, 58, 26, 54, 46, 33, 29, 1),
    (UUID(), @edinburghClub, @lockId, 'Callum Hunter-Hill', 0.30, 26, 55, 15, 46, 42, 44, 46, 1),
    (UUID(), @ulsterClub, @scrumhalfId, 'Dave Shanahan', 0.30, 51, 38, 53, 63, 44, 28, 31, 1),
    (UUID(), @stormersClub, @scrumhalfId, 'Dewaldt Duvenage', 0.30, 37, 48, 58, 84, 44, 32, 27, 1),
    (UUID(), @dragonsClub, @propId, 'Dylan Kelleher-Griffiths', 0.30, 35, 50, 18, 53, 44, 40, 38, 1),
    (UUID(), @stormersClub, @propId, 'Frans Malherbe', 0.30, 40, 58, 23, 62, 48, 24, 32, 1),
    (UUID(), @sharksClub, @flyhalfId, 'George Whitehead', 0.30, 45, 38, 66, 79, 42, 36, 24, 1),
    (UUID(), @scarletsClub, @propId, 'Harri O''Connor', 0.30, 31, 50, 20, 66, 44, 37, 35, 1),
    (UUID(), @ulsterClub, @hookerId, 'James McCormick', 0.30, 33, 56, 22, 66, 36, 36, 36, 1),
    (UUID(), @dragonsClub, @propId, 'Jordan Morris', 0.30, 34, 54, 21, 52, 41, 41, 37, 1),
    (UUID(), @cardiffClub, @propId, 'Keiron Assiratti', 0.30, 17, 57, 12, 19, 54, 56, 48, 1),
    (UUID(), @ospreysClub, @propId, 'Kian Hire', 0.30, 36, 54, 22, 53, 41, 33, 39, 1),
    (UUID(), @leinsterClub, @propId, 'Niall Smyth', 0.30, 39, 52, 23, 56, 43, 40, 28, 1),
    (UUID(), @sharksClub, @scrumhalfId, 'Ross Braude', 0.30, 48, 30, 47, 52, 40, 46, 34, 1),
    (UUID(), @stormersClub, @hookerId, 'Scarra Ntubeni', 0.30, 44, 54, 18, 70, 44, 30, 30, 1),
    (UUID(), @benettonClub, @lockId, 'Scott Scrafton', 0.30, 43, 55, 18, 40, 40, 34, 42, 1),
    (UUID(), @sharksClub, @flyhalfId, 'Siya Masuku', 0.30, 37, 39, 42, 87, 38, 43, 26, 1),
    (UUID(), @bullsClub, @propId, 'Sti Sithole', 0.30, 34, 57, 23, 68, 43, 27, 35, 1),
    (UUID(), @ospreysClub, @propId, 'Tom Botha', 0.30, 14, 48, 14, 58, 64, 59, 40, 1),
    (UUID(), @bullsClub, @hookerId, 'Akker van der Merwe', 0.20, 42, 44, 25, 60, 42, 35, 27, 1),
    (UUID(), @stormersClub, @propId, 'Ali Vermaak', 0.20, 28, 53, 26, 77, 42, 31, 27, 1),
    (UUID(), @sharksClub, @hookerId, 'Bongi Mbonambi', 0.20, 23, 42, 28, 59, 45, 48, 28, 1),
    (UUID(), @sharksClub, @scrumhalfId, 'Bradley Davids', 0.20, 35, 24, 55, 44, 47, 53, 32, 1),
    (UUID(), @ulsterClub, @propId, 'Bryan O''Connor', 0.20, 34, 51, 23, 55, 41, 34, 26, 1),
    (UUID(), @glasgowWarriorsClub, @lockId, 'Dylan Cockburn', 0.20, 33, 56, 15, 61, 40, 24, 40, 1),
    (UUID(), @zebreParmaClub, @propId, 'Enrique Pieretto', 0.20, 25, 51, 18, 39, 44, 52, 42, 1),
    (UUID(), @dragonsClub, @flyhalfId, 'Jac Lloyd', 0.20, 42, 37, 59, 61, 39, 35, 23, 1),
    (UUID(), @glasgowWarriorsClub, @scrumhalfId, 'Jack Oliver', 0.20, 40, 31, 60, 82, 41, 34, 25, 1),
    (UUID(), @bullsClub, @propId, 'Khutha Mchunu', 0.20, 37, 52, 17, 60, 40, 33, 28, 1),
    (UUID(), @zebreParmaClub, @propId, 'Luca Franceschetto', 0.20, 29, 55, 19, 55, 43, 35, 32, 1),
    (UUID(), @zebreParmaClub, @propId, 'Marcos Gallorini', 0.20, 17, 45, 21, 81, 49, 43, 28, 1),
    (UUID(), @benettonClub, @propId, 'Nahuel Tetaz Chaparro', 0.20, 33, 56, 21, 49, 43, 28, 26, 1),
    (UUID(), @stormersClub, @propId, 'Oli Kebble', 0.20, 26, 53, 20, 55, 45, 30, 34, 1),
    (UUID(), @dragonsClub, @propId, 'Owain James', 0.20, 30, 58, 18, 58, 44, 31, 26, 1),
    (UUID(), @dragonsClub, @propId, 'Rhodri Jones', 0.20, 28, 54, 21, 47, 45, 38, 32, 1),
    (UUID(), @sharksClub, @propId, 'Ruan Dreyer', 0.20, 29, 52, 15, 46, 45, 45, 27, 1),
    (UUID(), @benettonClub, @propId, 'Tiziano Pasquali', 0.20, 23, 48, 26, 58, 45, 43, 34, 1);


/* =====================================================================
   Squads for every manager who does not already have one.

   The five original demo teams keep their hand-picked squads: the simulated
   results, transfers, recommendations and leaderboard rows above all
   reference those exact players, so they are deliberately left untouched.

   The remaining teams were previously seeded empty. That made the demo
   misleading -- they appeared as league members, but a fixture can only be
   simulated when BOTH squads hold exactly 20 locked players, so every one of
   their fixtures was silently skipped with "does not have a complete
   20-player locked squad". Each now gets a full, distinct, valid squad.

   Composition (20 players, every position inside its min/max):
     Prop 3, Hooker 2, Lock 3, Loose Forward 3            = 11 forwards
     Scrum Half 2, Fly Half 2, Centre 2, Wing 2, Fullback 1 =  9 backs

   Squads are drawn by walking each position's player list at an offset
   derived from the team's index, so no two managers get the same XV and no
   manager gets the same player twice. Unavailable players are excluded.
   ===================================================================== */

INSERT INTO `team_player_selection`
    (selectionId, teamId, playerId, squadRole, isCaptain, is_vice_captain)
SELECT UUID(),
       pick.teamId,
       pick.playerId,
       /* Most valuable fifteen start, the rest cover the bench. */
       CASE WHEN pick.valueRank > 15 THEN 'BENCH' ELSE 'STARTING' END,
       pick.valueRank = 1,
       pick.valueRank = 2
FROM (SELECT squadless.teamId,
             pool.playerId,
             ROW_NUMBER() OVER (PARTITION BY squadless.teamId
                 ORDER BY pool.value DESC, pool.playerId) AS valueRank
      FROM (SELECT ft.teamId,
                   ROW_NUMBER() OVER (ORDER BY ft.teamName) - 1 AS teamIndex
            FROM `fantasyTeam` ft
            WHERE NOT EXISTS (SELECT 1
                              FROM `team_player_selection` existing
                              WHERE existing.teamId = ft.teamId)) AS squadless
               JOIN (SELECT quota.positionName, quota.required, slot.offsetInPosition
                     FROM (SELECT 'Prop' AS positionName, 3 AS required
                           UNION ALL
                           SELECT 'Hooker', 2
                           UNION ALL
                           SELECT 'Lock', 3
                           UNION ALL
                           SELECT 'Loose Forward', 3
                           UNION ALL
                           SELECT 'Scrum Half', 2
                           UNION ALL
                           SELECT 'Fly Half', 2
                           UNION ALL
                           SELECT 'Centre', 2
                           UNION ALL
                           SELECT 'Wing', 2
                           UNION ALL
                           SELECT 'Fullback', 1) AS quota
                              JOIN (SELECT 0 AS offsetInPosition
                                    UNION ALL
                                    SELECT 1
                                    UNION ALL
                                    SELECT 2) AS slot
                                   ON slot.offsetInPosition < quota.required) AS slots
               JOIN (SELECT pl.playerId,
                            pl.value,
                            po.positionName,
                            ROW_NUMBER() OVER (PARTITION BY po.positionName
                                ORDER BY pl.playerId) - 1 AS rankInPosition,
                            COUNT(*) OVER (PARTITION BY po.positionName) AS positionPool
                     FROM `player` pl
                              JOIN `position` po ON po.positionId = pl.positionId
                     WHERE NOT EXISTS (SELECT 1
                                       FROM `playerAvailability` pa
                                       WHERE pa.playerId = pl.playerId
                                         AND pa.status <> 'ACTIVE'
                                         AND (pa.endDate IS NULL OR pa.endDate >= CURDATE()))) AS pool
                    ON pool.positionName = slots.positionName
                        AND pool.rankInPosition =
                            ((squadless.teamIndex * slots.required) + slots.offsetInPosition)
                                % pool.positionPool) AS pick;

/*
    Keep every team's stored budget and validity honest: remainingBudget is
    the 196.00 starting budget less what the squad actually costs, and a team
    holding a complete squad is valid. Previously the empty teams claimed a
    full budget while being flagged invalid, and the original five carried
    hand-written budgets unrelated to their squad value.
*/
UPDATE `fantasyTeam` ft
    JOIN (SELECT s.teamId, SUM(p.value) AS squadValue, COUNT(*) AS squadSize
          FROM `team_player_selection` s
                   JOIN `player` p ON p.playerId = s.playerId
          GROUP BY s.teamId) AS totals ON totals.teamId = ft.teamId
SET ft.remainingBudget = 196.00 - totals.squadValue,
    ft.isValid         = (totals.squadSize = 20);

COMMIT;
