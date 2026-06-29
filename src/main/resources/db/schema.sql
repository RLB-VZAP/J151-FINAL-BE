CREATE
DATABASE IF NOT EXISTS `tryton_fantasy_rugby`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE
`tryton_fantasy_rugby`;

SET
FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `systemReport`;
DROP TABLE IF EXISTS `simulationSettings`;
DROP TABLE IF EXISTS `log`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `privateMessage`;
DROP TABLE IF EXISTS `chatMessage`;
DROP TABLE IF EXISTS `fantasyPoints`;
DROP TABLE IF EXISTS `scoringRule`;
DROP TABLE IF EXISTS `playerStatistics`;
DROP TABLE IF EXISTS `matchResult`;
DROP TABLE IF EXISTS `fixture`;
DROP TABLE IF EXISTS `ranking`;
DROP TABLE IF EXISTS `leaderboard`;
DROP TABLE IF EXISTS `leagueInvitation`;
DROP TABLE IF EXISTS `leagueMembership`;
DROP TABLE IF EXISTS `league`;
DROP TABLE IF EXISTS `locking`;
DROP TABLE IF EXISTS `transferHistory`;
DROP TABLE IF EXISTS `transfer`;
DROP TABLE IF EXISTS `playerRecommendation`;
DROP TABLE IF EXISTS `team_player_selection`;
DROP TABLE IF EXISTS `fantasyTeam`;
DROP TABLE IF EXISTS `performanceHistory`;
DROP TABLE IF EXISTS `playerAvailability`;
DROP TABLE IF EXISTS `player`;
DROP TABLE IF EXISTS `position`;
DROP TABLE IF EXISTS `club`;
DROP TABLE IF EXISTS `administrator`;
DROP TABLE IF EXISTS `leagueManager`;
DROP TABLE IF EXISTS `registeredUser`;
DROP TABLE IF EXISTS `user`;

SET
FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `user`
(
    `userId`           VARCHAR(36)  NOT NULL,
    `email`            VARCHAR(255) NOT NULL,
    `passwordHash`     VARCHAR(255) NOT NULL,
    `username`         VARCHAR(100) NOT NULL,
    `role`             ENUM('REGISTERED_USER','ADMINISTRATOR') NOT NULL DEFAULT 'REGISTERED_USER',
    `isActive`         BOOLEAN      NOT NULL DEFAULT TRUE,
    `profilePic`       VARCHAR(255)          DEFAULT NULL,
    `registrationDate` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_login_at`    DATETIME              DEFAULT NULL,
    PRIMARY KEY (`userId`),
    UNIQUE KEY `uk_user_email` (`email`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `registeredUser`
(
    `userId`             VARCHAR(36) NOT NULL,
    `registrationStatus` ENUM('PENDING','ACTIVE','SUSPENDED','DEACTIVATED') NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (`userId`),
    CONSTRAINT `fk_registeredUser_user`
        FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `administrator`
(
    `userId`     VARCHAR(36) NOT NULL,
    `adminLevel` INT         NOT NULL DEFAULT 1,
    PRIMARY KEY (`userId`),
    CONSTRAINT `fk_administrator_user`
        FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_administrator_adminLevel`
        CHECK (`adminLevel` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club`
(
    `clubId`         VARCHAR(36)  NOT NULL,
    `clubName`       VARCHAR(100) NOT NULL,
    `location`       VARCHAR(100)          DEFAULT NULL,
    `homeVenue`      VARCHAR(100)          DEFAULT NULL,
    `strengthRating` INT          NOT NULL DEFAULT 50,
    `isActive`       BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`clubId`),
    UNIQUE KEY `uk_club_clubName` (`clubName`),
    CONSTRAINT `chk_club_strengthRating`
        CHECK (`strengthRating` BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `position`
(
    `positionId`       VARCHAR(36) NOT NULL,
    `positionName`     VARCHAR(50) NOT NULL,
    `positionCategory` VARCHAR(50) NOT NULL,
    `minRequired`      INT         NOT NULL DEFAULT 0,
    `maxAllowed`       INT         NOT NULL DEFAULT 8,
    PRIMARY KEY (`positionId`),
    UNIQUE KEY `uk_position_positionName` (`positionName`),
    CONSTRAINT `chk_position_required_range`
        CHECK (`minRequired` >= 0 AND `maxAllowed` >= `minRequired`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `player`
(
    `playerId`             VARCHAR(36)    NOT NULL,
    `clubId`               VARCHAR(36)    NOT NULL,
    `positionId`           VARCHAR(36)    NOT NULL,
    `playerName`           VARCHAR(100)   NOT NULL,
    `value`                DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `attackingAbility`     INT            NOT NULL DEFAULT 50,
    `defensiveAbility`     INT            NOT NULL DEFAULT 50,
    `kickingAbility`       INT            NOT NULL DEFAULT 50,
    `discipline`           INT            NOT NULL DEFAULT 50,
    `consistency`          INT            NOT NULL DEFAULT 50,
    `fitness`              INT            NOT NULL DEFAULT 50,
    `currentForm`          INT            NOT NULL DEFAULT 50,
    `total_fantasy_points` INT            NOT NULL DEFAULT 0,
    `isActive`             BOOLEAN        NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`playerId`),
    KEY                    `idx_player_club` (`clubId`),
    KEY                    `idx_player_position` (`positionId`),
    KEY                    `idx_player_search` (`playerName`, `value`, `total_fantasy_points`),
    CONSTRAINT `fk_player_club`
        FOREIGN KEY (`clubId`) REFERENCES `club` (`clubId`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_player_position`
        FOREIGN KEY (`positionId`) REFERENCES `position` (`positionId`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `chk_player_value`
        CHECK (`value` >= 0),
    CONSTRAINT `chk_player_ratings`
        CHECK (
            `attackingAbility` BETWEEN 0 AND 100 AND
            `defensiveAbility` BETWEEN 0 AND 100 AND
            `kickingAbility` BETWEEN 0 AND 100 AND
            `discipline` BETWEEN 0 AND 100 AND
            `consistency` BETWEEN 0 AND 100 AND
            `fitness` BETWEEN 0 AND 100 AND
            `currentForm` BETWEEN 0 AND 100
            )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `playerAvailability`
(
    `availabilityId` VARCHAR(36) NOT NULL,
    `playerId`       VARCHAR(36) NOT NULL,
    `status`         ENUM('ACTIVE','INJURED','SUSPENDED','UNAVAILABLE','TRANSFERRED') NOT NULL DEFAULT 'ACTIVE',
    `effectiveDate`  DATE        NOT NULL DEFAULT (CURRENT_DATE),
    `endDate`        DATE                 DEFAULT NULL,
    `notes`          TEXT                 DEFAULT NULL,
    PRIMARY KEY (`availabilityId`),
    KEY              `idx_playerAvailability_player` (`playerId`),
    KEY              `idx_playerAvailability_status` (`status`),
    CONSTRAINT `fk_playerAvailability_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_playerAvailability_dates`
        CHECK (`endDate` IS NULL OR `endDate` >= `effectiveDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `performanceHistory`
(
    `performanceHistoryId` VARCHAR(36) NOT NULL,
    `playerId`             VARCHAR(36) NOT NULL,
    `season`               VARCHAR(20) NOT NULL,
    `roundNumber`          INT                  DEFAULT NULL,
    `matchesPlayed`        INT         NOT NULL DEFAULT 0,
    `tries`                INT         NOT NULL DEFAULT 0,
    `assists`              INT         NOT NULL DEFAULT 0,
    `tackles`              INT         NOT NULL DEFAULT 0,
    `fantasyPoints`        INT         NOT NULL DEFAULT 0,
    `formRating`           INT         NOT NULL DEFAULT 50,
    `recordedAt`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`performanceHistoryId`),
    KEY                    `idx_performanceHistory_player` (`playerId`),
    CONSTRAINT `fk_performanceHistory_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_performanceHistory_formRating`
        CHECK (`formRating` BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fantasyTeam`
(
    `teamId`           VARCHAR(36)    NOT NULL,
    `owner_user_id`    VARCHAR(36)    NOT NULL,
    `teamName`         VARCHAR(100)   NOT NULL,
    `total_team_value` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `remainingBudget`  DECIMAL(10, 2) NOT NULL DEFAULT 100.00,
    `creationDate`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `totalPoints`      INT            NOT NULL DEFAULT 0,
    `weeklyPoints`     INT            NOT NULL DEFAULT 0,
    `isValid`          BOOLEAN        NOT NULL DEFAULT FALSE,
    `isLocked`         BOOLEAN        NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`teamId`),
    UNIQUE KEY `uk_fantasyTeam_owner` (`owner_user_id`),
    UNIQUE KEY `uk_fantasyTeam_teamName` (`teamName`),
    CONSTRAINT `fk_fantasyTeam_owner`
        FOREIGN KEY (`owner_user_id`) REFERENCES `registeredUser` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_fantasyTeam_budget`
        CHECK (`total_team_value` >= 0 AND `remainingBudget` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_player_selection`
(
    `selectionId`     VARCHAR(36) NOT NULL,
    `teamId`          VARCHAR(36) NOT NULL,
    `playerId`        VARCHAR(36) NOT NULL,
    `selectedDate`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isCaptain`       BOOLEAN     NOT NULL DEFAULT FALSE,
    `is_vice_captain` BOOLEAN     NOT NULL DEFAULT FALSE,
    `isActive`        BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`selectionId`),
    UNIQUE KEY `uk_team_player_selection_active` (`teamId`, `playerId`, `isActive`),
    KEY               `idx_team_player_selection_player` (`playerId`),
    CONSTRAINT `fk_team_player_selection_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_team_player_selection_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `chk_team_player_selection_captains`
        CHECK (NOT (`isCaptain` = TRUE AND `is_vice_captain` = TRUE))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `playerRecommendation`
(
    `recommendationId`      VARCHAR(36)   NOT NULL,
    `teamId`                VARCHAR(36)   NOT NULL,
    `current_player_id`     VARCHAR(36)            DEFAULT NULL,
    `recommended_player_id` VARCHAR(36)   NOT NULL,
    `reason`                TEXT                   DEFAULT NULL,
    `score`                 DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    `createdAt`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isDismissed`           BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`recommendationId`),
    KEY                     `idx_playerRecommendation_team` (`teamId`),
    KEY                     `idx_playerRecommendation_current_player` (`current_player_id`),
    KEY                     `idx_playerRecommendation_recommended_player` (`recommended_player_id`),
    CONSTRAINT `fk_playerRecommendation_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_playerRecommendation_current_player`
        FOREIGN KEY (`current_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_playerRecommendation_recommended_player`
        FOREIGN KEY (`recommended_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transfer`
(
    `transferId`             VARCHAR(36) NOT NULL,
    `teamId`                 VARCHAR(36) NOT NULL,
    `removed_player_id`      VARCHAR(36)          DEFAULT NULL,
    `added_player_id`        VARCHAR(36)          DEFAULT NULL,
    `transferDate`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `penaltyApplied`         BOOLEAN     NOT NULL DEFAULT FALSE,
    `penaltyPoints`          INT         NOT NULL DEFAULT 0,
    `transfer_window_status` ENUM('OPEN','CLOSED','LOCKED') NOT NULL DEFAULT 'OPEN',
    `roundNumber`            INT                  DEFAULT NULL,
    `confirmed`              BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`transferId`),
    KEY                      `idx_transfer_team` (`teamId`),
    KEY                      `idx_transfer_removed_player` (`removed_player_id`),
    KEY                      `idx_transfer_added_player` (`added_player_id`),
    CONSTRAINT `fk_transfer_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_transfer_removed_player`
        FOREIGN KEY (`removed_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_transfer_added_player`
        FOREIGN KEY (`added_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `chk_transfer_has_player_change`
        CHECK (`removed_player_id` IS NOT NULL OR `added_player_id` IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transferHistory`
(
    `transferHistoryId`    VARCHAR(36) NOT NULL,
    `transferId`           VARCHAR(36) NOT NULL,
    `teamId`               VARCHAR(36) NOT NULL,
    `removed_player_id`    VARCHAR(36)          DEFAULT NULL,
    `added_player_id`      VARCHAR(36)          DEFAULT NULL,
    `old_team_value`       DECIMAL(10, 2)       DEFAULT NULL,
    `new_team_value`       DECIMAL(10, 2)       DEFAULT NULL,
    `old_remaining_budget` DECIMAL(10, 2)       DEFAULT NULL,
    `new_remaining_budget` DECIMAL(10, 2)       DEFAULT NULL,
    `penaltyPoints`        INT         NOT NULL DEFAULT 0,
    `createdAt`            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`transferHistoryId`),
    KEY                    `idx_transferHistory_transfer` (`transferId`),
    KEY                    `idx_transferHistory_team` (`teamId`),
    CONSTRAINT `fk_transferHistory_transfer`
        FOREIGN KEY (`transferId`) REFERENCES `transfer` (`transferId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_transferHistory_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_transferHistory_removed_player`
        FOREIGN KEY (`removed_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_transferHistory_added_player`
        FOREIGN KEY (`added_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `league`
(
    `leagueId` VARCHAR(36) NOT NULL,
    `manager_user_id` VARCHAR(36) DEFAULT NULL,
    `leagueName` VARCHAR(100) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `leagueType` ENUM('PUBLIC','PRIVATE') NOT NULL DEFAULT 'PUBLIC',
    `leagueCode` VARCHAR(6) DEFAULT NULL,
    `creationDate` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isActive` BOOLEAN NOT NULL DEFAULT TRUE,
    `maxMembers` INT NOT NULL DEFAULT 100,
    PRIMARY KEY (`leagueId`),
    UNIQUE KEY `uk_league_code` (`leagueCode`),
    KEY `idx_league_manager` (`manager_user_id`),
    KEY `idx_league_type_active` (`leagueType`, `isActive`),
    CONSTRAINT `fk_league_manager`
        FOREIGN KEY (`manager_user_id`) REFERENCES `registeredUser` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `chk_league_maxMembers`
        CHECK (`maxMembers` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leagueMembership`
(
    `membershipId`       VARCHAR(36) NOT NULL,
    `leagueId`           VARCHAR(36) NOT NULL,
    `registered_user_id` VARCHAR(36) NOT NULL,
    `teamId`             VARCHAR(36) NOT NULL,
    `isActive`           BOOLEAN     NOT NULL DEFAULT TRUE,
    `joinDate`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `memberRole`         ENUM('MEMBER','MANAGER') NOT NULL DEFAULT 'MEMBER',
    PRIMARY KEY (`membershipId`),
    UNIQUE KEY `uk_leagueMembership_user` (`leagueId`, `registered_user_id`),
    UNIQUE KEY `uk_leagueMembership_team` (`leagueId`, `teamId`),
    KEY                  `idx_leagueMembership_user` (`registered_user_id`),
    KEY                  `idx_leagueMembership_team` (`teamId`),
    CONSTRAINT `fk_leagueMembership_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_leagueMembership_registeredUser`
        FOREIGN KEY (`registered_user_id`) REFERENCES `registeredUser` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_leagueMembership_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leagueInvitation`
(
    `invitationId` VARCHAR(36) NOT NULL,
    `leagueId` VARCHAR(36) NOT NULL,
    `invited_user_id` VARCHAR(36) DEFAULT NULL,
    `created_by_user_id` VARCHAR(36) DEFAULT NULL,
    `expiryDate` DATETIME NOT NULL,
    `expired` BOOLEAN NOT NULL DEFAULT FALSE,
    `acceptedAt` DATETIME DEFAULT NULL,
    PRIMARY KEY (`invitationId`),
    KEY `idx_leagueInvitation_league` (`leagueId`),
    KEY `idx_leagueInvitation_invited_user` (`invited_user_id`),
    CONSTRAINT `fk_leagueInvitation_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_leagueInvitation_invited_user`
        FOREIGN KEY (`invited_user_id`) REFERENCES `registeredUser` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_leagueInvitation_created_by`
        FOREIGN KEY (`created_by_user_id`) REFERENCES `registeredUser` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `chk_leagueInvitation_expiry`
        CHECK (`expiryDate` > `acceptedAt` OR `acceptedAt` IS NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--is_master_leaderboard is for if the reference is the overall leaderboard for the season, or if it is for a specific league
CREATE TABLE `leaderboard`
(
    `leaderboardId`         VARCHAR(36) NOT NULL,
    `leagueId`              VARCHAR(36)          DEFAULT NULL,
    `lastUpdated`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `season`                VARCHAR(20) NOT NULL,
    `is_master_leaderboard` BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`leaderboardId`),
    UNIQUE KEY `uk_leaderboard_league_season` (`leagueId`, `season`),
    KEY                     `idx_leaderboard_master` (`is_master_leaderboard`),
    CONSTRAINT `fk_leaderboard_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ranking`
(
    `rankingId`       VARCHAR(36) NOT NULL,
    `leaderboardId`   VARCHAR(36) NOT NULL,
    `teamId`          VARCHAR(36) NOT NULL,
    `currentRanking`  INT         NOT NULL,
    `previousRanking` INT                  DEFAULT NULL,
    `weeklyScore`     INT         NOT NULL DEFAULT 0,
    `totalScore`      INT         NOT NULL DEFAULT 0,
    `rankMovement`    INT         NOT NULL DEFAULT 0,
    `updatedAt`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`rankingId`),
    UNIQUE KEY `uk_ranking_leaderboard_team` (`leaderboardId`, `teamId`),
    UNIQUE KEY `uk_ranking_position` (`leaderboardId`, `currentRanking`),
    KEY               `idx_ranking_team` (`teamId`),
    CONSTRAINT `fk_ranking_leaderboard`
        FOREIGN KEY (`leaderboardId`) REFERENCES `leaderboard` (`leaderboardId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_ranking_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_ranking_current`
        CHECK (`currentRanking` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fixture`
(
    `fixtureId`          VARCHAR(36)  NOT NULL,
    `leagueId`           VARCHAR(36)           DEFAULT NULL,
    `home_club_id`       VARCHAR(36)  NOT NULL,
    `away_club_id`       VARCHAR(36)  NOT NULL,
    `matchDate`          DATE         NOT NULL,
    `matchTime`          TIME         NOT NULL,
    `venue`              VARCHAR(100) NOT NULL,
    `status`             ENUM('UPCOMING','LOCKED','LIVE','COMPLETED','POSTPONED','CANCELLED','SIMULATED') NOT NULL DEFAULT 'UPCOMING',
    `isSimulated`        BOOLEAN      NOT NULL DEFAULT FALSE,
    `isLocked`           BOOLEAN      NOT NULL DEFAULT FALSE,
    `match_round_number` INT          NOT NULL,
    `lockDeadline`       DATETIME              DEFAULT NULL,
    PRIMARY KEY (`fixtureId`),
    KEY                  `idx_fixture_league` (`leagueId`),
    KEY                  `idx_fixture_home_club` (`home_club_id`),
    KEY                  `idx_fixture_away_club` (`away_club_id`),
    KEY                  `idx_fixture_status_date` (`status`, `matchDate`, `matchTime`),
    CONSTRAINT `fk_fixture_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_fixture_home_club`
        FOREIGN KEY (`home_club_id`) REFERENCES `club` (`clubId`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_fixture_away_club`
        FOREIGN KEY (`away_club_id`) REFERENCES `club` (`clubId`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `chk_fixture_clubs_different`
        CHECK (`home_club_id` <> `away_club_id`),
    CONSTRAINT `chk_fixture_round`
        CHECK (`match_round_number` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `matchResult`
(
    `resultId`                  VARCHAR(36) NOT NULL,
    `fixtureId`                 VARCHAR(36) NOT NULL,
    `homeScore`                 INT         NOT NULL DEFAULT 0,
    `awayScore`                 INT         NOT NULL DEFAULT 0,
    `resultDate`                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `approved`                  BOOLEAN     NOT NULL DEFAULT FALSE,
    `approved_by_admin_user_id` VARCHAR(36)          DEFAULT NULL,
    `simulation_run_number`     INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`resultId`),
    UNIQUE KEY `uk_matchResult_fixture` (`fixtureId`),
    KEY                         `idx_matchResult_approved_by` (`approved_by_admin_user_id`),
    CONSTRAINT `fk_matchResult_fixture`
        FOREIGN KEY (`fixtureId`) REFERENCES `fixture` (`fixtureId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_matchResult_approved_by_admin`
        FOREIGN KEY (`approved_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `chk_matchResult_scores`
        CHECK (`homeScore` >= 0 AND `awayScore` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `playerStatistics`
(
    `statId`                    VARCHAR(36) NOT NULL,
    `fixtureId`                 VARCHAR(36) NOT NULL,
    `playerId`                  VARCHAR(36) NOT NULL,
    `tries`                     INT         NOT NULL DEFAULT 0,
    `assists`                   INT         NOT NULL DEFAULT 0,
    `tackles`                   INT         NOT NULL DEFAULT 0,
    `missedTackles`             INT         NOT NULL DEFAULT 0,
    `conversions`               INT         NOT NULL DEFAULT 0,
    `penalties`                 INT         NOT NULL DEFAULT 0,
    `metersGained`              INT         NOT NULL DEFAULT 0,
    `yellowCards`               INT         NOT NULL DEFAULT 0,
    `redCards`                  INT         NOT NULL DEFAULT 0,
    `statisticDate`             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `captured_by_admin_user_id` VARCHAR(36)          DEFAULT NULL,
    PRIMARY KEY (`statId`),
    UNIQUE KEY `uk_playerStatistics_fixture_player` (`fixtureId`, `playerId`),
    KEY                         `idx_playerStatistics_player` (`playerId`),
    KEY                         `idx_playerStatistics_captured_by` (`captured_by_admin_user_id`),
    CONSTRAINT `fk_playerStatistics_fixture`
        FOREIGN KEY (`fixtureId`) REFERENCES `fixture` (`fixtureId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_playerStatistics_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_playerStatistics_captured_by_admin`
        FOREIGN KEY (`captured_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `chk_playerStatistics_non_negative`
        CHECK (
            `tries` >= 0 AND `assists` >= 0 AND `tackles` >= 0 AND
            `missedTackles` >= 0 AND `conversions` >= 0 AND
            `penalties` >= 0 AND `metersGained` >= 0 AND
            `yellowCards` >= 0 AND `redCards` >= 0
            )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `scoringRule`
(
    `ruleId`        VARCHAR(36) NOT NULL,
    `leagueId`      VARCHAR(36)          DEFAULT NULL,
    `eventType`     VARCHAR(50) NOT NULL,
    `pointsAwarded` INT         NOT NULL,
    `isDeduction`   BOOLEAN     NOT NULL DEFAULT FALSE,
    `description`   TEXT                 DEFAULT NULL,
    `isActive`      BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`ruleId`),
    UNIQUE KEY `uk_scoringRule_league_event` (`leagueId`, `eventType`),
    KEY             `idx_scoringRule_league` (`leagueId`),
    CONSTRAINT `fk_scoringRule_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fantasyPoints`
(
    `pointsId`           VARCHAR(36) NOT NULL,
    `teamId`             VARCHAR(36) NOT NULL,
    `playerId`           VARCHAR(36) NOT NULL,
    `fixtureId`          VARCHAR(36) NOT NULL,
    `ruleId`             VARCHAR(36)          DEFAULT NULL,
    `pointsEarned`       INT         NOT NULL DEFAULT 0,
    `calculationDate`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `match_round_number` INT         NOT NULL,
    `calculationVersion` INT         NOT NULL DEFAULT 1,
    PRIMARY KEY (`pointsId`),
    UNIQUE KEY `uk_fantasyPoints_source` (`teamId`, `playerId`, `fixtureId`, `ruleId`, `calculationVersion`),
    KEY                  `idx_fantasyPoints_player` (`playerId`),
    KEY                  `idx_fantasyPoints_fixture` (`fixtureId`),
    KEY                  `idx_fantasyPoints_rule` (`ruleId`),
    CONSTRAINT `fk_fantasyPoints_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_fantasyPoints_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_fantasyPoints_fixture`
        FOREIGN KEY (`fixtureId`) REFERENCES `fixture` (`fixtureId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_fantasyPoints_scoringRule`
        FOREIGN KEY (`ruleId`) REFERENCES `scoringRule` (`ruleId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `chk_fantasyPoints_round`
        CHECK (`match_round_number` > 0 AND `calculationVersion` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chatMessage`
(
    `messageId`          VARCHAR(36) NOT NULL,
    `leagueId`           VARCHAR(36) NOT NULL,
    `sender_user_id`     VARCHAR(36) NOT NULL,
    `content`            TEXT        NOT NULL,
    `sentDate`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `removed`            BOOLEAN     NOT NULL DEFAULT FALSE,
    `removed_by_user_id` VARCHAR(36)          DEFAULT NULL,
    PRIMARY KEY (`messageId`),
    KEY                  `idx_chatMessage_league_sent` (`leagueId`, `sentDate`),
    KEY                  `idx_chatMessage_sender` (`sender_user_id`),
    KEY                  `idx_chatMessage_removed_by` (`removed_by_user_id`),
    CONSTRAINT `fk_chatMessage_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_chatMessage_sender`
        FOREIGN KEY (`sender_user_id`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_chatMessage_removed_by`
        FOREIGN KEY (`removed_by_user_id`) REFERENCES `user` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `privateMessage`
(
    `privateMessageId` VARCHAR(36) NOT NULL,
    `sender_user_id`   VARCHAR(36) NOT NULL,
    `receiver_user_id` VARCHAR(36) NOT NULL,
    `content`          TEXT        NOT NULL,
    `sentDate`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `removed`          BOOLEAN     NOT NULL DEFAULT FALSE,
    `isRead`           BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`privateMessageId`),
    KEY                `idx_privateMessage_sender` (`sender_user_id`),
    KEY                `idx_privateMessage_receiver` (`receiver_user_id`),
    CONSTRAINT `fk_privateMessage_sender`
        FOREIGN KEY (`sender_user_id`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_privateMessage_receiver`
        FOREIGN KEY (`receiver_user_id`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_privateMessage_users_different`
        CHECK (`sender_user_id` <> `receiver_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `report`
(
    `reportId`                  VARCHAR(36) NOT NULL,
    `reporter_user_id`          VARCHAR(36) NOT NULL,
    `messageId`                 VARCHAR(36)          DEFAULT NULL,
    `reported_user_id`          VARCHAR(36)          DEFAULT NULL,
    `reportReason`              TEXT        NOT NULL,
    `reportDate`                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status`                    ENUM('OPEN','UNDER_REVIEW','RESOLVED','REJECTED') NOT NULL DEFAULT 'OPEN',
    `resolved`                  BOOLEAN     NOT NULL DEFAULT FALSE,
    `resolution`                TEXT                 DEFAULT NULL,
    `resolved_by_admin_user_id` VARCHAR(36)          DEFAULT NULL,
    PRIMARY KEY (`reportId`),
    KEY                         `idx_report_reporter` (`reporter_user_id`),
    KEY                         `idx_report_message` (`messageId`),
    KEY                         `idx_report_reported_user` (`reported_user_id`),
    KEY                         `idx_report_status` (`status`),
    CONSTRAINT `fk_report_reporter`
        FOREIGN KEY (`reporter_user_id`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_report_chatMessage`
        FOREIGN KEY (`messageId`) REFERENCES `chatMessage` (`messageId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_report_reported_user`
        FOREIGN KEY (`reported_user_id`) REFERENCES `user` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_report_resolved_by_admin`
        FOREIGN KEY (`resolved_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification`
(
    `notificationId`      VARCHAR(36) NOT NULL,
    `userId`              VARCHAR(36) NOT NULL,
    `type`                ENUM('CHAT_MESSAGE','LEADERBOARD_CHANGE','POINTS_UPDATE','SIMULATED_RESULT','PLAYER_AVAILABILITY','TRANSFER_DEADLINE','LEAGUE_INVITATION','REPORT_UPDATE','SYSTEM') NOT NULL,
    `body`                TEXT        NOT NULL,
    `createdAt`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isRead`              BOOLEAN     NOT NULL DEFAULT FALSE,
    `related_entity_type` VARCHAR(50)          DEFAULT NULL,
    `related_entity_id`   VARCHAR(36)          DEFAULT NULL,
    PRIMARY KEY (`notificationId`),
    KEY                   `idx_notification_user_read` (`userId`, `isRead`, `createdAt`),
    CONSTRAINT `fk_notification_user`
        FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `log`
(
    `logId`          VARCHAR(36) NOT NULL,
    `userId`         VARCHAR(36)          DEFAULT NULL,
    `transferId`     VARCHAR(36)          DEFAULT NULL,
    `notificationId` VARCHAR(36)          DEFAULT NULL,
    `entityType`     VARCHAR(50) NOT NULL,
    `entityId`       VARCHAR(36)          DEFAULT NULL,
    `actionType`     VARCHAR(50) NOT NULL,
    `description`    TEXT                 DEFAULT NULL,
    `createdAt`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ipAddress`      VARCHAR(45)          DEFAULT NULL,
    PRIMARY KEY (`logId`),
    KEY              `idx_log_user` (`userId`),
    KEY              `idx_log_transfer` (`transferId`),
    KEY              `idx_log_notification` (`notificationId`),
    KEY              `idx_log_entity` (`entityType`, `entityId`),
    KEY              `idx_log_createdAt` (`createdAt`),
    CONSTRAINT `fk_log_user`
        FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_log_transfer`
        FOREIGN KEY (`transferId`) REFERENCES `transfer` (`transferId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_log_notification`
        FOREIGN KEY (`notificationId`) REFERENCES `notification` (`notificationId`)
            ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `locking`
(
    `lockId`        VARCHAR(36) NOT NULL,
    `teamId`        VARCHAR(36)          DEFAULT NULL,
    `playerId`      VARCHAR(36)          DEFAULT NULL,
    `fixtureId`     VARCHAR(36)          DEFAULT NULL,
    `admin_user_id` VARCHAR(36)          DEFAULT NULL,
    `transferId`    VARCHAR(36)          DEFAULT NULL,
    `lockedAt`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `unlockAt`      DATETIME             DEFAULT NULL,
    `isLocked`      BOOLEAN     NOT NULL DEFAULT TRUE,
    `reason`        TEXT                 DEFAULT NULL,
    PRIMARY KEY (`lockId`),
    KEY             `idx_locking_team` (`teamId`),
    KEY             `idx_locking_player` (`playerId`),
    KEY             `idx_locking_fixture` (`fixtureId`),
    KEY             `idx_locking_admin` (`admin_user_id`),
    KEY             `idx_locking_transfer` (`transferId`),
    CONSTRAINT `fk_locking_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_locking_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_locking_fixture`
        FOREIGN KEY (`fixtureId`) REFERENCES `fixture` (`fixtureId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_locking_admin`
        FOREIGN KEY (`admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_locking_transfer`
        FOREIGN KEY (`transferId`) REFERENCES `transfer` (`transferId`)
            ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `chk_locking_has_target`
        CHECK (`teamId` IS NOT NULL OR `playerId` IS NOT NULL OR `fixtureId` IS NOT NULL),
    CONSTRAINT `chk_locking_unlock_after_lock`
        CHECK (`unlockAt` IS NULL OR `unlockAt` >= `lockedAt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `simulationSettings`
(
    `simulationSettingsId`    VARCHAR(36)   NOT NULL,
    `leagueId`                VARCHAR(36)            DEFAULT NULL,
    `home_advantage_weight`   DECIMAL(5, 2) NOT NULL DEFAULT 5.00,
    `club_strength_weight`    DECIMAL(5, 2) NOT NULL DEFAULT 30.00,
    `player_form_weight`      DECIMAL(5, 2) NOT NULL DEFAULT 25.00,
    `team_balance_weight`     DECIMAL(5, 2) NOT NULL DEFAULT 20.00,
    `random_variation_weight` DECIMAL(5, 2) NOT NULL DEFAULT 20.00,
    `require_admin_approval`  BOOLEAN       NOT NULL DEFAULT TRUE,
    `allow_resimulation`      BOOLEAN       NOT NULL DEFAULT FALSE,
    `maxResimulations`        INT           NOT NULL DEFAULT 0,
    `isActive`                BOOLEAN       NOT NULL DEFAULT TRUE,
    `createdAt`               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt`               DATETIME               DEFAULT NULL,
    PRIMARY KEY (`simulationSettingsId`),
    KEY                       `idx_simulationSettings_league` (`leagueId`),
    CONSTRAINT `fk_simulationSettings_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `chk_simulationSettings_resimulations`
        CHECK (`maxResimulations` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `systemReport`
(
    `systemReportId`             VARCHAR(36)  NOT NULL,
    `generated_by_admin_user_id` VARCHAR(36)           DEFAULT NULL,
    `reportType`                 ENUM('ACTIVE_USERS','ACTIVE_LEAGUES','TOP_FANTASY_TEAMS','TOP_RUGBY_PLAYERS','MOST_SELECTED_PLAYERS','UNAVAILABLE_PLAYERS','COMPLETED_FIXTURES','SIMULATED_FIXTURES','TRANSFER_ACTIVITY','LEAGUE_CHAT_ACTIVITY','SYSTEM_ACTIVITY') NOT NULL,
    `reportTitle`                VARCHAR(150) NOT NULL,
    `parametersJson`             JSON                  DEFAULT NULL,
    `resultJson`                 JSON                  DEFAULT NULL,
    `generatedAt`                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`systemReportId`),
    KEY                          `idx_systemReport_generated_by` (`generated_by_admin_user_id`),
    KEY                          `idx_systemReport_type_date` (`reportType`, `generatedAt`),
    CONSTRAINT `fk_systemReport_generated_by_admin`
        FOREIGN KEY (`generated_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;