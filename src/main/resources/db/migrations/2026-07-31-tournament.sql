/*
    Migration: Rugby World Cup style tournaments.

    Brings an existing `tryton_fantasy_rugby` database up to the tournament
    schema WITHOUT dropping data. `schema.sql` already contains all of this and
    remains the source of truth for a fresh build; this file exists so a
    running database can be upgraded in place.

    Safe to run once. Re-running will fail on the CREATE TABLE / ADD COLUMN
    statements, which is intentional -- it is a migration, not an idempotent
    bootstrap.

    Usage:
      mysql -u <user> -p tryton_fantasy_rugby < 2026-07-31-tournament.sql
*/

USE `tryton_fantasy_rugby`;

-- ---------------------------------------------------------------------
-- 1. League lifecycle
-- ---------------------------------------------------------------------

ALTER TABLE `league`
    ADD COLUMN `status` ENUM ('FORMING', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'FORMING' AFTER `maxMembers`,
    ADD COLUMN `startedAt` DATETIME DEFAULT NULL AFTER `status`,
    ADD KEY `idx_league_status` (`status`),
    ADD CONSTRAINT `chk_league_started`
        CHECK (
            (`status` = 'FORMING' AND `startedAt` IS NULL)
                OR (`status` <> 'FORMING' AND `startedAt` IS NOT NULL)
            );

-- ---------------------------------------------------------------------
-- 2. Tournament tables
-- ---------------------------------------------------------------------

CREATE TABLE `tournament`
(
    `tournamentId`        VARCHAR(36) NOT NULL,
    `leagueId`            VARCHAR(36) NOT NULL,
    `season`              VARCHAR(20) NOT NULL,
    `status`              ENUM('POOL_STAGE', 'KNOCKOUT_STAGE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'POOL_STAGE',

    `managerCount`        INT         NOT NULL,
    `poolCount`           INT         NOT NULL,
    `poolMatchdays`       INT         NOT NULL,
    `bracketSize`         INT         NOT NULL,
    `thirdPlacePlayoff`   BOOLEAN     NOT NULL DEFAULT TRUE,

    `champion_team_id`    VARCHAR(36)          DEFAULT NULL,
    `runner_up_team_id`   VARCHAR(36)          DEFAULT NULL,
    `third_place_team_id` VARCHAR(36)          DEFAULT NULL,

    `createdAt`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `completedAt`         DATETIME             DEFAULT NULL,

    PRIMARY KEY (`tournamentId`),
    UNIQUE KEY `uk_tournament_league_season` (`leagueId`, `season`),
    KEY                   `idx_tournament_status` (`status`),

    CONSTRAINT `fk_tournament_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_tournament_champion`
        FOREIGN KEY (`champion_team_id`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_tournament_runner_up`
        FOREIGN KEY (`runner_up_team_id`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_tournament_third_place`
        FOREIGN KEY (`third_place_team_id`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT `chk_tournament_managers`
        CHECK (`managerCount` BETWEEN 2 AND 100),
    CONSTRAINT `chk_tournament_pools`
        CHECK (`poolCount` >= 1 AND `poolMatchdays` >= 1),
    CONSTRAINT `chk_tournament_bracket`
        CHECK (`bracketSize` >= 2 AND `bracketSize` <= `managerCount`),
    CONSTRAINT `chk_tournament_completion`
        CHECK (
            (`status` = 'COMPLETED' AND `completedAt` IS NOT NULL)
                OR (`status` <> 'COMPLETED' AND `completedAt` IS NULL)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `tournament_pool`
(
    `poolId`       VARCHAR(36) NOT NULL,
    `tournamentId` VARCHAR(36) NOT NULL,
    `poolName`     VARCHAR(8)  NOT NULL,
    `poolSize`     INT         NOT NULL,
    `createdAt`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`poolId`),
    UNIQUE KEY `uk_tournament_pool_name` (`tournamentId`, `poolName`),

    CONSTRAINT `fk_tournament_pool_tournament`
        FOREIGN KEY (`tournamentId`) REFERENCES `tournament` (`tournamentId`)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `chk_tournament_pool_size`
        CHECK (`poolSize` BETWEEN 2 AND 5)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `tournament_pool_member`
(
    `poolMemberId` VARCHAR(36) NOT NULL,
    `tournamentId` VARCHAR(36) NOT NULL,
    `poolId`       VARCHAR(36) NOT NULL,
    `teamId`       VARCHAR(36) NOT NULL,
    `seed`         INT         NOT NULL,
    `createdAt`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`poolMemberId`),
    UNIQUE KEY `uk_tournament_member_team` (`tournamentId`, `teamId`),
    UNIQUE KEY `uk_tournament_pool_member` (`poolId`, `teamId`),
    KEY            `idx_tournament_member_pool` (`poolId`),

    CONSTRAINT `fk_tournament_member_tournament`
        FOREIGN KEY (`tournamentId`) REFERENCES `tournament` (`tournamentId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_tournament_member_pool`
        FOREIGN KEY (`poolId`) REFERENCES `tournament_pool` (`poolId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_tournament_member_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `chk_tournament_member_seed`
        CHECK (`seed` > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `tournament_standing`
(
    `standingId`       VARCHAR(36) NOT NULL,
    `tournamentId`     VARCHAR(36) NOT NULL,
    `poolId`           VARCHAR(36) NOT NULL,
    `teamId`           VARCHAR(36) NOT NULL,

    `played`           INT         NOT NULL DEFAULT 0,
    `won`              INT         NOT NULL DEFAULT 0,
    `drawn`            INT         NOT NULL DEFAULT 0,
    `lost`             INT         NOT NULL DEFAULT 0,

    `pointsFor`        INT         NOT NULL DEFAULT 0,
    `pointsAgainst`    INT         NOT NULL DEFAULT 0,
    `pointsDifference` INT GENERATED ALWAYS AS (`pointsFor` - `pointsAgainst`) STORED,

    `attackBonus`      INT         NOT NULL DEFAULT 0,
    `losingBonus`      INT         NOT NULL DEFAULT 0,
    `tournamentPoints` INT         NOT NULL DEFAULT 0,

    `position`         INT                  DEFAULT NULL,
    `qualified`        BOOLEAN     NOT NULL DEFAULT FALSE,
    `updatedAt`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`standingId`),
    UNIQUE KEY `uk_tournament_standing_team` (`poolId`, `teamId`),
    KEY                `idx_tournament_standing_order` (`poolId`, `tournamentPoints`, `pointsDifference`),

    CONSTRAINT `fk_tournament_standing_tournament`
        FOREIGN KEY (`tournamentId`) REFERENCES `tournament` (`tournamentId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_tournament_standing_pool`
        FOREIGN KEY (`poolId`) REFERENCES `tournament_pool` (`poolId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_tournament_standing_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `chk_tournament_standing_counts`
        CHECK (`played` = `won` + `drawn` + `lost`),
    CONSTRAINT `chk_tournament_standing_points`
        CHECK (`tournamentPoints` >= 0 AND `attackBonus` >= 0 AND `losingBonus` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `tournament_settings`
(
    `settingsId`             VARCHAR(36) NOT NULL,
    `win_points`             INT         NOT NULL DEFAULT 4,
    `draw_points`            INT         NOT NULL DEFAULT 2,
    `loss_points`            INT         NOT NULL DEFAULT 0,
    `attack_bonus_threshold` INT         NOT NULL DEFAULT 190,
    `losing_bonus_margin`    INT         NOT NULL DEFAULT 7,
    `third_place_playoff`    BOOLEAN     NOT NULL DEFAULT TRUE,
    `updatedAt`              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`settingsId`),

    CONSTRAINT `chk_tournament_settings_points`
        CHECK (
            `win_points` >= `draw_points`
                AND `draw_points` >= `loss_points`
                AND `loss_points` >= 0
            ),
    CONSTRAINT `chk_tournament_settings_bonus`
        CHECK (`attack_bonus_threshold` > 0 AND `losing_bonus_margin` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO `tournament_settings` (`settingsId`)
VALUES (UUID());

-- ---------------------------------------------------------------------
-- 3. Tournament wiring on `fixture`
-- ---------------------------------------------------------------------

ALTER TABLE `fixture`
    ADD COLUMN `tournamentId` VARCHAR(36) DEFAULT NULL AFTER `simulationDate`,
    ADD COLUMN `poolId` VARCHAR(36) DEFAULT NULL AFTER `tournamentId`,
    ADD COLUMN `stage` ENUM('POOL', 'ROUND_OF_32', 'ROUND_OF_16', 'QUARTER_FINAL', 'SEMI_FINAL', 'THIRD_PLACE', 'FINAL') DEFAULT NULL AFTER `poolId`,
    ADD COLUMN `bracketSlot` INT DEFAULT NULL AFTER `stage`,
    ADD COLUMN `matchdayNumber` INT DEFAULT NULL AFTER `bracketSlot`,
    ADD KEY `idx_fixture_tournament_stage` (`tournamentId`, `stage`, `bracketSlot`),
    ADD KEY `idx_fixture_pool` (`poolId`),
    ADD CONSTRAINT `fk_fixture_tournament`
        FOREIGN KEY (`tournamentId`) REFERENCES `tournament` (`tournamentId`)
            ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_fixture_pool`
        FOREIGN KEY (`poolId`) REFERENCES `tournament_pool` (`poolId`)
            ON DELETE CASCADE ON UPDATE CASCADE;

-- ---------------------------------------------------------------------
-- 4. Triggers
-- ---------------------------------------------------------------------

DELIMITER $$

CREATE TRIGGER `trg_tournament_completion_update`
    BEFORE UPDATE
    ON `tournament`
    FOR EACH ROW
BEGIN
    IF NEW.`status` = 'COMPLETED' AND NEW.`champion_team_id` IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A completed tournament must record a champion';
END IF;

    IF
NEW.`status` <> 'COMPLETED' AND NEW.`champion_team_id` IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only a completed tournament may record a champion';
END IF;
END$$

CREATE TRIGGER `trg_tournament_member_integrity_insert`
    BEFORE INSERT
    ON `tournament_pool_member`
    FOR EACH ROW
BEGIN
    DECLARE v_leagueId VARCHAR(36);

    IF NOT EXISTS (
        SELECT 1 FROM `tournament_pool`
        WHERE `poolId` = NEW.`poolId`
          AND `tournamentId` = NEW.`tournamentId`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Pool does not belong to the stated tournament';
END IF;

SELECT `leagueId`
INTO v_leagueId
FROM `tournament`
WHERE `tournamentId` = NEW.`tournamentId`;

IF
NOT EXISTS (
        SELECT 1 FROM `leagueMembership`
        WHERE `leagueId` = v_leagueId
          AND `teamId` = NEW.`teamId`
          AND `isActive` = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A tournament pool member must be an active league member';
END IF;
END$$

CREATE TRIGGER `trg_fixture_tournament_integrity_insert`
    BEFORE INSERT
    ON `fixture`
    FOR EACH ROW
BEGIN
    IF (NEW.`tournamentId` IS NULL) <> (NEW.`stage` IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A tournament fixture must carry a stage, and a stage requires a tournament';
END IF;

    IF
NEW.`stage` = 'POOL' AND NEW.`poolId` IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A pool fixture must belong to a pool';
END IF;

    IF
(NEW.`stage` IS NULL OR NEW.`stage` <> 'POOL') AND NEW.`poolId` IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only a pool fixture may belong to a pool';
END IF;

    IF
NEW.`tournamentId` IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1 FROM `tournament`
            WHERE `tournamentId` = NEW.`tournamentId`
              AND `leagueId` = NEW.`leagueId`
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Fixture tournament belongs to a different league';
END IF;

        IF
NEW.`poolId` IS NOT NULL AND NOT EXISTS (
            SELECT 1 FROM `tournament_pool`
            WHERE `poolId` = NEW.`poolId`
              AND `tournamentId` = NEW.`tournamentId`
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Fixture pool belongs to a different tournament';
END IF;
END IF;
END$$

DELIMITER ;
