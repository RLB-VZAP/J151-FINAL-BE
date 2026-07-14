USE tryton_fantasy_rugby;

/*
    TryTons lecturer demo data
    Run schema.sql first, then run this file.

    Demo logins:
      Administrator: admin@tritan.com / Admin@12345
      Registered user: john@test.com / John@12345
*/

-- -----------------------------------------------------------------------------
-- Users
-- -----------------------------------------------------------------------------
INSERT INTO `user`
    (`userId`, `email`, `passwordHash`, `username`, `role`, `isActive`, `profilePic`)
VALUES
    ('10000000-0000-0000-0000-000000000001',
     'admin@tritan.com',
     '$2a$12$ToCAGmBUmoJd5p1LWWewHeCDD8sy/lQmYzUUCv9Wf701EtxoqIpIC',
     'admin', 'ADMINISTRATOR', TRUE, NULL),
    ('10000000-0000-0000-0000-000000000002',
     'john@test.com',
     '$2a$12$BwiRPiDDJb81VBpIJ.5D4u/xpaAY9fqM/PJzqsDAz703vHwPEXt4W',
     'john', 'REGISTERED_USER', TRUE, NULL)
ON DUPLICATE KEY UPDATE
    `passwordHash` = VALUES(`passwordHash`),
    `username` = VALUES(`username`),
    `role` = VALUES(`role`),
    `isActive` = VALUES(`isActive`);

INSERT INTO `administrator` (`userId`, `adminLevel`)
VALUES ('10000000-0000-0000-0000-000000000001', 5)
ON DUPLICATE KEY UPDATE `adminLevel` = VALUES(`adminLevel`);

INSERT INTO `registeredUser` (`userId`, `registrationStatus`)
VALUES ('10000000-0000-0000-0000-000000000002', 'ACTIVE')
ON DUPLICATE KEY UPDATE `registrationStatus` = VALUES(`registrationStatus`);

-- -----------------------------------------------------------------------------
-- Clubs
-- -----------------------------------------------------------------------------
INSERT INTO `club` (`clubId`, `clubName`, `location`, `homeVenue`, `isActive`)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'Bulls', 'Pretoria', 'Loftus Versfeld', TRUE),
    ('20000000-0000-0000-0000-000000000002', 'Sharks', 'Durban', 'Hollywoodbets Kings Park', TRUE),
    ('20000000-0000-0000-0000-000000000003', 'Stormers', 'Cape Town', 'DHL Stadium', TRUE),
    ('20000000-0000-0000-0000-000000000004', 'Lions', 'Johannesburg', 'Emirates Airline Park', TRUE)
ON DUPLICATE KEY UPDATE
    `clubName` = VALUES(`clubName`),
    `location` = VALUES(`location`),
    `homeVenue` = VALUES(`homeVenue`),
    `isActive` = VALUES(`isActive`);

-- -----------------------------------------------------------------------------
-- Positions
-- -----------------------------------------------------------------------------
INSERT INTO `position`
    (`positionId`, `positionName`, `positionCategory`, `minRequired`, `maxAllowed`)
VALUES
    ('21000000-0000-0000-0000-000000000001', 'Prop', 'FORWARD', 2, 4),
    ('21000000-0000-0000-0000-000000000002', 'Hooker', 'FORWARD', 1, 2),
    ('21000000-0000-0000-0000-000000000003', 'Lock', 'FORWARD', 2, 4),
    ('21000000-0000-0000-0000-000000000004', 'Flanker', 'FORWARD', 2, 4),
    ('21000000-0000-0000-0000-000000000005', 'Number Eight', 'FORWARD', 1, 2),
    ('21000000-0000-0000-0000-000000000006', 'Scrum Half', 'BACK', 1, 2),
    ('21000000-0000-0000-0000-000000000007', 'Fly Half', 'BACK', 1, 2),
    ('21000000-0000-0000-0000-000000000008', 'Centre', 'BACK', 2, 4),
    ('21000000-0000-0000-0000-000000000009', 'Wing', 'BACK', 2, 4),
    ('21000000-0000-0000-0000-000000000010', 'Fullback', 'BACK', 1, 2)
ON DUPLICATE KEY UPDATE
    `positionName` = VALUES(`positionName`),
    `positionCategory` = VALUES(`positionCategory`),
    `minRequired` = VALUES(`minRequired`),
    `maxAllowed` = VALUES(`maxAllowed`);

-- -----------------------------------------------------------------------------
-- Players
-- -----------------------------------------------------------------------------
INSERT INTO `player`
    (`playerId`, `clubId`, `positionId`, `playerName`, `value`,
     `attackingAbility`, `defensiveAbility`, `kickingAbility`,
     `discipline`, `consistency`, `fitness`, `currentForm`, `isActive`)
VALUES
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000007', 'Johan van Wyk', 12.50, 88, 72, 91, 80, 85, 90, 87, TRUE),
    ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000009', 'Chris Botha', 10.50, 85, 70, 60, 78, 80, 88, 82, TRUE),
    ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000003', 'Pieter Smith', 9.00, 60, 90, 20, 88, 82, 92, 79, TRUE),
    ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000001', 'Franco Adams', 8.00, 50, 92, 10, 87, 78, 90, 75, TRUE),

    ('30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000007', 'Ryan Williams', 13.00, 90, 73, 92, 81, 88, 90, 89, TRUE),
    ('30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000008', 'Luke Daniels', 11.50, 84, 83, 50, 84, 80, 87, 83, TRUE),
    ('30000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000002', 'Jason Brown', 8.50, 65, 89, 10, 88, 81, 90, 78, TRUE),
    ('30000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000004', 'Grant White', 9.20, 72, 91, 20, 86, 84, 91, 82, TRUE),

    ('30000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000007', 'Peter Adams', 13.50, 91, 76, 93, 85, 88, 92, 90, TRUE),
    ('30000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000009', 'Kyle Petersen', 10.80, 89, 67, 40, 82, 79, 89, 84, TRUE),
    ('30000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000003', 'Dean Miller', 8.90, 58, 93, 10, 87, 82, 91, 80, TRUE),
    ('30000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000010', 'Neil Thomas', 11.20, 85, 74, 80, 84, 83, 88, 85, TRUE),

    ('30000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000004', '21000000-0000-0000-0000-000000000006', 'Morne Venter', 10.00, 78, 74, 70, 82, 81, 88, 80, TRUE),
    ('30000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000004', '21000000-0000-0000-0000-000000000008', 'Ruan Smith', 10.50, 81, 80, 40, 84, 79, 85, 79, TRUE),
    ('30000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000004', '21000000-0000-0000-0000-000000000004', 'Willem Botha', 9.00, 68, 90, 10, 86, 80, 90, 78, TRUE),
    ('30000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000004', '21000000-0000-0000-0000-000000000001', 'Hendrik Fourie', 8.10, 52, 92, 5, 88, 81, 91, 76, TRUE)
ON DUPLICATE KEY UPDATE
    `clubId` = VALUES(`clubId`),
    `positionId` = VALUES(`positionId`),
    `playerName` = VALUES(`playerName`),
    `value` = VALUES(`value`),
    `attackingAbility` = VALUES(`attackingAbility`),
    `defensiveAbility` = VALUES(`defensiveAbility`),
    `kickingAbility` = VALUES(`kickingAbility`),
    `discipline` = VALUES(`discipline`),
    `consistency` = VALUES(`consistency`),
    `fitness` = VALUES(`fitness`),
    `currentForm` = VALUES(`currentForm`),
    `isActive` = VALUES(`isActive`);

-- -----------------------------------------------------------------------------
-- Current availability
-- -----------------------------------------------------------------------------
INSERT INTO `playerAvailability`
    (`availabilityId`, `playerId`, `status`, `effectiveDate`, `endDate`, `notes`)
VALUES
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', 'INJURED', CURRENT_DATE, NULL, 'Minor training injury'),
    ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000004', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000005', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000006', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000007', 'SUSPENDED', CURRENT_DATE, NULL, 'Demo suspension'),
    ('40000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000008', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000009', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000010', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000011', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000012', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000013', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000014', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000015', '30000000-0000-0000-0000-000000000015', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability'),
    ('40000000-0000-0000-0000-000000000016', '30000000-0000-0000-0000-000000000016', 'ACTIVE', CURRENT_DATE, NULL, 'Demo availability')
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `effectiveDate` = VALUES(`effectiveDate`),
    `endDate` = VALUES(`endDate`),
    `notes` = VALUES(`notes`);

SELECT 'TryTons demo seed loaded successfully.' AS `message`;
