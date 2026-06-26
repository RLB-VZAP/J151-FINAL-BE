    USE
    tritan_fantasy_rugby;

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

    INSERT INTO user
        (userId, email, passwordHash, username, displayName, role)
    VALUES (@adminId, '[admin@tritan.com](mailto:admin@tritan.com)', '$2a$10$seedhash', 'admin', 'System Administrator',
            'ADMINISTRATOR'),
           (@johnId, '[john@test.com](mailto:john@test.com)', '$2a$10$seedhash', 'john', 'John Smith', 'REGISTERED_USER'),
           (@sarahId, '[sarah@test.com](mailto:sarah@test.com)', '$2a$10$seedhash', 'sarah', 'Sarah Jones',
            'REGISTERED_USER'),
           (@mikeId, '[mike@test.com](mailto:mike@test.com)', '$2a$10$seedhash', 'mike', 'Mike Adams', 'REGISTERED_USER'),
           (@emmaId, '[emma@test.com](mailto:emma@test.com)', '$2a$10$seedhash', 'emma', 'Emma Wilson', 'REGISTERED_USER'),
           (@davidId, '[david@test.com](mailto:david@test.com)', '$2a$10$seedhash', 'david', 'David Brown',
            'REGISTERED_USER'),
           (@lisaId, '[lisa@test.com](mailto:lisa@test.com)', '$2a$10$seedhash', 'lisa', 'Lisa Taylor', 'REGISTERED_USER'),
           (@tomId, '[tom@test.com](mailto:tom@test.com)', '$2a$10$seedhash', 'tom', 'Tom White', 'REGISTERED_USER');

    INSERT INTO administrator(userId, adminLevel)
    VALUES (@adminId, 5);

    INSERT INTO registeredUser(userId, registrationStatus)
    VALUES (@johnId, 'ACTIVE'),
           (@sarahId, 'ACTIVE'),
           (@mikeId, 'ACTIVE'),
           (@emmaId, 'ACTIVE'),
           (@davidId, 'ACTIVE'),
           (@lisaId, 'ACTIVE'),
           (@tomId, 'ACTIVE');


    SET
    @bullsClub = UUID();
    SET
    @sharksClub = UUID();
    SET
    @stormersClub = UUID();
    SET
    @lionsClub = UUID();
    SET
    @cheetahsClub = UUID();
    SET
    @pumasClub = UUID();

    INSERT INTO club
        (clubId, clubName, location, homeVenue, strengthRating)
    VALUES (@bullsClub, 'Bulls', 'Pretoria', 'Loftus Versfeld', 88),
           (@sharksClub, 'Sharks', 'Durban', 'Kings Park', 85),
           (@stormersClub, 'Stormers', 'Cape Town', 'DHL Stadium', 90),
           (@lionsClub, 'Lions', 'Johannesburg', 'Ellis Park', 82),
           (@cheetahsClub, 'Cheetahs', 'Bloemfontein', 'Free State Stadium', 78),
           (@pumasClub, 'Pumas', 'Nelspruit', 'Mbombela Stadium', 75);

    SET
    @propId = UUID();
    SET
    @hookerId = UUID();
    SET
    @lockId = UUID();
    SET
    @flankerId = UUID();
    SET
    @number8Id = UUID();
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

    INSERT INTO position
        (positionId, positionName, positionCategory, minRequired, maxAllowed)
    VALUES (@propId, 'Prop', 'FORWARD', 2, 4),
           (@hookerId, 'Hooker', 'FORWARD', 1, 2),
           (@lockId, 'Lock', 'FORWARD', 2, 4),
           (@flankerId, 'Flanker', 'FORWARD', 2, 4),
           (@number8Id, 'Number Eight', 'FORWARD', 1, 2),
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

    INSERT INTO player
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
           (@p10, @sharksClub, @flankerId, 'Grant White', 9.2, 72, 91, 20, 86, 84, 91, 82),

           (@p11, @stormersClub, @flyhalfId, 'Peter Adams', 13.5, 91, 76, 93, 85, 88, 92, 90),
           (@p12, @stormersClub, @centreId, 'Mark Taylor', 11.0, 82, 82, 50, 84, 80, 88, 81),
           (@p13, @stormersClub, @wingId, 'Kyle Petersen', 10.8, 89, 67, 40, 82, 79, 89, 84),
           (@p14, @stormersClub, @lockId, 'Dean Miller', 8.9, 58, 93, 10, 87, 82, 91, 80),
           (@p15, @stormersClub, @fullbackId, 'Neil Thomas', 11.2, 85, 74, 80, 84, 83, 88, 85),

           (@p16, @lionsClub, @flyhalfId, 'Morne Venter', 12.0, 84, 71, 89, 80, 82, 86, 81),
           (@p17, @lionsClub, @centreId, 'Ruan Smith', 10.5, 81, 80, 40, 84, 79, 85, 79),
           (@p18, @lionsClub, @wingId, 'Jaco Meyer', 10.1, 86, 66, 30, 80, 77, 87, 80),
           (@p19, @lionsClub, @flankerId, 'Willem Botha', 9.0, 68, 90, 10, 86, 80, 90, 78),
           (@p20, @lionsClub, @propId, 'Hendrik Fourie', 8.1, 52, 92, 5, 88, 81, 91, 76),

           (@p21, @cheetahsClub, @flyhalfId, 'Stefan Ross', 11.5, 82, 69, 85, 79, 80, 84, 78),
           (@p22, @cheetahsClub, @centreId, 'Chris Nel', 10.2, 80, 78, 40, 82, 78, 84, 77),
           (@p23, @cheetahsClub, @wingId, 'Brandon Visser', 9.8, 84, 65, 20, 80, 77, 85, 76),
           (@p24, @cheetahsClub, @hookerId, 'Paul Kruger', 8.0, 60, 87, 10, 86, 80, 89, 75),
           (@p25, @cheetahsClub, @lockId, 'Jacques Swanepoel', 8.4, 55, 89, 5, 87, 81, 90, 76),

           (@p26, @pumasClub, @flyhalfId, 'Kevin Roberts', 11.0, 80, 68, 82, 78, 79, 83, 75),
           (@p27, @pumasClub, @centreId, 'Sean Peters', 9.9, 78, 76, 35, 80, 77, 84, 74),
           (@p28, @pumasClub, @wingId, 'Alan Brooks', 9.5, 82, 64, 15, 79, 76, 85, 73),
           (@p29, @pumasClub, @flankerId, 'Dylan Green', 8.6, 66, 88, 10, 84, 80, 89, 75),
           (@p30, @pumasClub, @fullbackId, 'Ethan Lewis', 10.4, 81, 72, 78, 82, 79, 86, 77);

    INSERT INTO playerAvailability
        (availabilityId, playerId, status, effectiveDate)
    VALUES (UUID(), @p1, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p2, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p3, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p4, 'INJURED', CURRENT_DATE()),
           (UUID(), @p5, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p6, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p7, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p8, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p9, 'SUSPENDED', CURRENT_DATE()),
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
           (UUID(), @p23, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p24, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p25, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p26, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p27, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p28, 'ACTIVE', CURRENT_DATE()),
           (UUID(), @p29, 'SUSPENDED', CURRENT_DATE()),
           (UUID(), @p30, 'ACTIVE', CURRENT_DATE());

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

    INSERT INTO fantasyTeam
    (teamId,
     owner_user_id,
     teamName,
     total_team_value,
     remainingBudget,
     totalPoints,
     weeklyPoints,
     isValid)
    VALUES (@team1, @johnId, 'John Warriors', 95.00, 5.00, 250, 45, TRUE),
           (@team2, @sarahId, 'Sarah Sharks', 92.50, 7.50, 225, 40, TRUE),
           (@team3, @mikeId, 'Mike Titans', 90.00, 10.00, 210, 35, TRUE),
           (@team4, @emmaId, 'Emma Eagles', 94.00, 6.00, 240, 42, TRUE),
           (@team5, @davidId, 'David Dragons', 89.00, 11.00, 200, 30, TRUE);

    INSERT INTO team_player_selection
        (selectionId, teamId, playerId, isCaptain, is_vice_captain)
    VALUES (UUID(), @team1, @p1, TRUE, FALSE),
           (UUID(), @team1, @p3, FALSE, TRUE),
           (UUID(), @team1, @p7, FALSE, FALSE),
           (UUID(), @team1, @p11, FALSE, FALSE),
           (UUID(), @team1, @p15, FALSE, FALSE),

           (UUID(), @team2, @p6, TRUE, FALSE),
           (UUID(), @team2, @p8, FALSE, TRUE),
           (UUID(), @team2, @p10, FALSE, FALSE),
           (UUID(), @team2, @p12, FALSE, FALSE),
           (UUID(), @team2, @p21, FALSE, FALSE),

           (UUID(), @team3, @p16, TRUE, FALSE),
           (UUID(), @team3, @p17, FALSE, TRUE),
           (UUID(), @team3, @p19, FALSE, FALSE),
           (UUID(), @team3, @p22, FALSE, FALSE),
           (UUID(), @team3, @p26, FALSE, FALSE),

           (UUID(), @team4, @p11, TRUE, FALSE),
           (UUID(), @team4, @p13, FALSE, TRUE),
           (UUID(), @team4, @p14, FALSE, FALSE),
           (UUID(), @team4, @p24, FALSE, FALSE),
           (UUID(), @team4, @p30, FALSE, FALSE),

           (UUID(), @team5, @p21, TRUE, FALSE),
           (UUID(), @team5, @p23, FALSE, TRUE),
           (UUID(), @team5, @p25, FALSE, FALSE),
           (UUID(), @team5, @p27, FALSE, FALSE),
           (UUID(), @team5, @p28, FALSE, FALSE);

    SET
    @publicLeague = UUID();
    SET
    @privateLeague = UUID();

    INSERT INTO league
    (leagueId,
     manager_user_id,
     leagueName,
     description,
     leagueType,
     maxMembers)
    VALUES (@publicLeague,
            @johnId,
            'Global Fantasy Rugby',
            'Official public league for all players',
            'PUBLIC',
            100);

    INSERT INTO league
    (leagueId,
     manager_user_id,
     leagueName,
     description,
     leagueType,
     leagueCode,
     maxMembers)
    VALUES (@privateLeague,
            @sarahId,
            'Friends Rugby League',
            'Private invite-only league',
            'PRIVATE',
            'ABC123',
            20);


    INSERT INTO leagueMembership
    (membershipId,
     leagueId,
     registered_user_id,
     teamId,
     memberRole)
    VALUES (UUID(), @publicLeague, @johnId, @team1, 'MANAGER'),
           (UUID(), @publicLeague, @sarahId, @team2, 'MEMBER'),
           (UUID(), @publicLeague, @mikeId, @team3, 'MEMBER'),
           (UUID(), @publicLeague, @emmaId, @team4, 'MEMBER'),
           (UUID(), @publicLeague, @davidId, @team5, 'MEMBER'),

           (UUID(), @privateLeague, @sarahId, @team2, 'MANAGER'),
           (UUID(), @privateLeague, @johnId, @team1, 'MEMBER'),
           (UUID(), @privateLeague, @mikeId, @team3, 'MEMBER');

    INSERT INTO leagueInvitation
    (invitationId,
     leagueId,
     invited_user_id,
     created_by_user_id,
     expiryDate)
    VALUES (UUID(),
            @privateLeague,
            @lisaId,
            @sarahId,
            DATE_ADD(NOW(), INTERVAL 7 DAY)),
           (UUID(),
            @privateLeague,
            @tomId,
            @sarahId,
            DATE_ADD(NOW(), INTERVAL 7 DAY));

    SET
    @fixture1 = UUID();
    SET
    @fixture2 = UUID();
    SET
    @fixture3 = UUID();
    SET
    @fixture4 = UUID();
    SET
    @fixture5 = UUID();
    SET
    @fixture6 = UUID();

    INSERT INTO fixture
    (fixtureId,
     home_club_id,
     away_club_id,
     matchDate,
     matchTime,
     venue,
     status,
     match_round_number)
    VALUES (@fixture1,
            @bullsClub,
            @sharksClub,
            '2026-07-01',
            '15:00:00',
            'Loftus Versfeld',
            'UPCOMING',
            1),

           (@fixture2,
            @stormersClub,
            @lionsClub,
            '2026-07-01',
            '18:00:00',
            'DHL Stadium',
            'UPCOMING',
            1),

           (@fixture3,
            @cheetahsClub,
            @pumasClub,
            '2026-07-02',
            '16:00:00',
            'Free State Stadium',
            'UPCOMING',
            1),

           (@fixture4,
            @sharksClub,
            @stormersClub,
            '2026-07-08',
            '15:00:00',
            'Kings Park',
            'UPCOMING',
            2),

           (@fixture5,
            @bullsClub,
            @lionsClub,
            '2026-07-08',
            '18:00:00',
            'Loftus Versfeld',
            'UPCOMING',
            2),

           (@fixture6,
            @pumasClub,
            @cheetahsClub,
            '2026-07-09',
            '16:00:00',
            'Mbombela Stadium',
            'UPCOMING',
            2);

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

    INSERT INTO scoringRule
    (ruleId,
     eventType,
     pointsAwarded,
     isDeduction,
     description)
    VALUES (@ruleTry,
            'TRY',
            5,
            FALSE,
            'Points awarded for scoring a try'),

           (@ruleAssist,
            'ASSIST',
            3,
            FALSE,
            'Points awarded for a try assist'),

           (@ruleTackle,
            'TACKLE',
            1,
            FALSE,
            'Points awarded for a successful tackle'),

           (@ruleConversion,
            'CONVERSION',
            2,
            FALSE,
            'Points awarded for a successful conversion'),

           (@ruleMissedTackle,
            'MISSED_TACKLE',
            1,
            TRUE,
            'Deduction for a missed tackle'),

           (@ruleYellowCard,
            'YELLOW_CARD',
            3,
            TRUE,
            'Deduction for a yellow card'),

           (@ruleRedCard,
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

    INSERT INTO leaderboard
    (leaderboardId,
     leagueId,
     season,
     is_master_leaderboard)
    VALUES (@masterLeaderboard,
            NULL,
            '2026',
            TRUE),
           (@publicLeaderboard,
            @publicLeague,
            '2026',
            FALSE),
           (@privateLeaderboard,
            @privateLeague,
            '2026',
            FALSE);

    INSERT INTO ranking
    (rankingId,
     leaderboardId,
     teamId,
     currentRanking,
     previousRanking,
     weeklyScore,
     totalScore,
     rankMovement)
    VALUES (UUID(), @masterLeaderboard, @team1, 1, NULL, 45, 250, 0),
           (UUID(), @masterLeaderboard, @team4, 2, NULL, 42, 240, 0),
           (UUID(), @masterLeaderboard, @team2, 3, NULL, 40, 225, 0),
           (UUID(), @masterLeaderboard, @team3, 4, NULL, 35, 210, 0),
           (UUID(), @masterLeaderboard, @team5, 5, NULL, 30, 200, 0),

           (UUID(), @publicLeaderboard, @team1, 1, NULL, 45, 250, 0),
           (UUID(), @publicLeaderboard, @team4, 2, NULL, 42, 240, 0),
           (UUID(), @publicLeaderboard, @team2, 3, NULL, 40, 225, 0),
           (UUID(), @publicLeaderboard, @team3, 4, NULL, 35, 210, 0),
           (UUID(), @publicLeaderboard, @team5, 5, NULL, 30, 200, 0),

           (UUID(), @privateLeaderboard, @team2, 1, NULL, 40, 225, 0),
           (UUID(), @privateLeaderboard, @team1, 2, NULL, 45, 250, 0),
           (UUID(), @privateLeaderboard, @team3, 3, NULL, 35, 210, 0);

    SET
    @chat1 = UUID();
    SET
    @chat2 = UUID();
    SET
    @chat3 = UUID();

    INSERT INTO chatMessage
    (messageId,
     leagueId,
     sender_user_id,
     content)
    VALUES (@chat1,
            @publicLeague,
            @johnId,
            'Welcome to the Global Fantasy Rugby League!'),

           (@chat2,
            @publicLeague,
            @sarahId,
            'Good luck everyone this season.'),

           (@chat3,
            @privateLeague,
            @sarahId,
            'Welcome to the private friends league.');

    INSERT INTO notification
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
            'LEAGUE_INVITATION',
            'You have invited new members.',
            'LEAGUE',
            @privateLeague),

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

    INSERT INTO report
    (reportId,
     reporter_user_id,
     messageId,
     reported_user_id,
     reportReason,
     status)
    VALUES (UUID(),
            @johnId,
            @chat2,
            @sarahId,
            'Test moderation report',
            'OPEN');

    INSERT INTO simulationSettings
    (simulationSettingsId,
     leagueId,
     home_advantage_weight,
     club_strength_weight,
     player_form_weight,
     team_balance_weight,
     random_variation_weight,
     require_admin_approval,
     allow_resimulation,
     maxResimulations,
     isActive)
    VALUES (UUID(),
            NULL,
            5.00,
            30.00,
            25.00,
            20.00,
            20.00,
            TRUE,
            FALSE,
            0,
            TRUE),

           (UUID(),
            @publicLeague,
            5.00,
            35.00,
            25.00,
            20.00,
            15.00,
            TRUE,
            TRUE,
            3,
            TRUE);


    SET
    @result1 = UUID();

    INSERT INTO matchResult
    (resultId,
     fixtureId,
     homeScore,
     awayScore,
     approved,
     approved_by_admin_user_id,
     simulation_run_number)
    VALUES (@result1,
            @fixture1,
            28,
            24,
            TRUE,
            @adminId,
            1);

    INSERT INTO playerStatistics
    (statId,
     fixtureId,
     playerId,
     tries,
     assists,
     tackles,
     metersGained,
     captured_by_admin_user_id)
    VALUES (UUID(),
            @fixture1,
            @p1,
            1,
            1,
            7,
            120,
            @adminId),

           (UUID(),
            @fixture1,
            @p6,
            2,
            0,
            4,
            150,
            @adminId);

    INSERT INTO fantasyPoints
    (pointsId,
     teamId,
     playerId,
     fixtureId,
     ruleId,
     pointsEarned,
     match_round_number)
    VALUES (UUID(),
            @team1,
            @p1,
            @fixture1,
            @ruleTry,
            5,
            1),

           (UUID(),
            @team1,
            @p1,
            @fixture1,
            @ruleAssist,
            3,
            1),

           (UUID(),
            @team2,
            @p6,
            @fixture1,
            @ruleTry,
            10,
            1);

    INSERT INTO systemReport
    (systemReportId,
     generated_by_admin_user_id,
     reportType,
     reportTitle)
    VALUES (UUID(),
            @adminId,
            'ACTIVE_USERS',
            'Active Users Report');

    INSERT INTO log
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

