# Fantasy TryTons / TriTans Fantasy Rugby League — Corrected 3-Tier Stateless MVC Task Plan

This document outlines the project tickets using the corrected architecture: a separated **frontend WAR** and **backend WAR** in a **3-tier stateless MVC structure**.

## Architecture Rule

### Frontend WAR / View Layer

The frontend WAR contains JSP pages, HTML, CSS, JavaScript, frontend servlets where form handling is needed, and REST client classes that call the backend API.

Correct frontend flow:

- `JSP/Page`
- `Frontend Servlet`
- `REST Client`
- `Backend REST Resource`

Frontend must not:

- Connect directly to the database
- Use DAOs
- Contain final business validation rules
- Hash passwords instead of the backend
- Decide final permissions

### Backend WAR / API Layer

The backend WAR contains JAX-RS REST Resources, service classes, DAO/repository classes, model classes, DTO/request/response classes, utilities, enums, and database setup files.

Correct backend flow:

- `Backend REST Resource`
- `Service`
- `DAO / Repository`
- `MySQL Database`

Backend REST Resources must not contain SQL or large business logic. Services contain business rules and validation. DAOs contain SQL and database mapping only.

### Security Rule

Passwords must be securely one-way hashed using `BCrypt` through `PasswordUtil`. `PasswordUtil` is the project wrapper for BCrypt hashing and verification, so services should call `PasswordUtil` rather than scattering direct BCrypt calls across the codebase. Do not store plain text passwords. Do not expose passwords, BCrypt hashes, or salts in DTOs or API responses. BCrypt hashes include their salt and cost information in the stored hash string, so do not create or expose a separate salt value unless the team explicitly agrees to a different design.


## How to Use This Task Plan

Each active or restructured ticket keeps the original task intent, but the handoff detail has been tightened so that developers know where the work belongs and what it depends on. Completed Week 1 backend tickets are intentionally left in their historical format because that work is already done.

**Do not move work across layers.** A task may mention files in more than one layer only when the feature needs both a backend API and a frontend page/client. The backend remains the final authority for validation, permissions, scoring, ownership, and database updates.

**Important ticket-reading rule:**

- **MUST CREATE** = brand-new files, classes, pages, scripts, DTOs, or documents that this ticket owner must add.
- **MUST EDIT** = existing files, classes, pages, scripts, DTOs, or documents that this ticket owner must change. Do not recreate these as duplicates.
- **MUST USE / WAIT FOR FROM OTHER TICKETS** = items this ticket depends on. The assigned developer must pull the latest branch, import/use these items, call the owner ticket's code, or wait until the owner ticket is finished. Dependency and handoff detail belongs in this section for active/restructured tickets.

Create and edit are not interchangeable. If an item is listed under **MUST CREATE**, the developer owns creating it. If an item is listed under **MUST EDIT**, the developer must change the existing item and must not create a duplicate with a similar name.

**Recommended package style:** use the project team's existing base package, then keep responsibilities separated, for example:

- Backend WAR: `resource`, `service`, `dao`, `model`, `dto`, `filter`, `util`, `config`
- Frontend WAR: `servlet`, `client`, view model/helper classes only when a ticket lists them under **MUST CREATE**, JSP pages, CSS/Tailwind, JavaScript

**Ticket maintenance rule:** when a class name changes, update the ticket's **MUST CREATE**, **MUST EDIT**, **MUST USE / WAIT FOR FROM OTHER TICKETS**, and any referenced test checklist in the same commit or PR.

## Create vs Edit Rule

For every ticket, developers must follow the section names exactly:

- Items under **MUST CREATE** are new files/classes/pages/scripts/documents that the ticket owner must add.
- Items under **MUST EDIT** already exist or are expected to exist before this ticket is started. The developer must update those files/classes in place.
- Items under **MUST USE / WAIT FOR FROM OTHER TICKETS** are not owned by this ticket. The developer must not recreate them.

This rule is important because two developers creating similar versions of the same class will cause merge conflicts, duplicate logic, and broken dependencies.

## Layer Responsibility Summary

| Layer | Allowed Responsibility | Must Not Do |
|---|---|---|
| JSP / HTML / CSS / JS | Display forms, tables, messages, navigation, and simple UX checks | SQL, DAO calls, final validation, final permissions |
| Frontend Servlet | Receive browser form requests and call frontend REST clients | SQL, DAO calls, password hashing, business rules |
| Frontend REST Client | Send HTTP requests to backend API and return response data to frontend servlets/pages | Decide final success/failure without backend |
| Backend REST Resource | Accept JSON requests, call services, return JSON responses | SQL, large business rules, direct JSP rendering |
| Service | Business rules, validation, ownership checks, scoring, coordination between DAOs | Raw HTML/JSP display logic |
| DAO / Repository | SQL only, mapping rows to models/DTOs | Business decisions, permission decisions |
| Database Scripts | Schema, constraints, seed data | Java request handling |

## High-Level Dependency Order

1. Completed Week 1 backend foundation remains the base for all active tickets.
2. `W1-FE01A` to `W1-FE01C` prepare the frontend auth flow after backend auth exists.
3. `W2-T01A` to `W2-T01E` create player, club, and position backend support before player browsing and squad selection.
4. `W2-T03A` to `W2-T04A` create fantasy-team persistence and validation before fantasy-team Resources and frontend pages.
5. `W2-T06A` to `W2-T07A` create league and membership support before leaderboards, chat, and moderation.
6. `W3-T01A` to `W3-T04B` create fixture, result, statistic, and scoring-rule data before point calculation.
7. `W3-T05A` to `W3-T06B` calculate points before score history and refreshed leaderboards.
8. Week 4 social/admin polish depends on authentication, leagues, membership checks, reporting support, and complete setup/docs.

## Final-Project Reality Check

- This repository is the backend WAR repository. Backend Java code, SQL scripts, backend test evidence, and shared backend-facing documents belong here. Frontend JSP/servlet/client tasks still matter for the overall project, but they belong in the separate frontend WAR/repository.
- The final project is not complete just because the backend compiles. Final hand-in readiness requires a working backend WAR, working frontend WAR, reproducible database reset/seed flow, agreed auth contract, API contract documentation, demo accounts, and setup/test evidence that another person can follow.
- Before Week 2 begins, the team must lock three shared integration decisions and keep them consistent in code and docs: the backend auth mechanism, the backend error response format, and the frontend-to-backend base URL/configuration approach.

## Requirement Traceability Summary

| Requirement Area | Main Tickets Covering It |
|---|---|
| Register, login, profile/security foundation | completed `W1-BE05`, `W1-BE06`, `W1-BE07`, `W1-BE11`, plus active `W1-FE01A` to `W1-FE01C` |
| Database, models, DAO foundation | completed `W1-BE01`, `W1-BE02`, `W1-BE03`, `W1-BE04`, `W1-BE08` |
| Players, clubs, positions, searching | `W2-T01A` to `W2-T01E`, `W2-T02A` to `W2-T02B` |
| Fantasy team creation and validation | `W2-T03A` to `W2-T03D`, `W2-T04A`, `W2-T05A` to `W2-T05B` |
| Leagues, membership, leaderboards | `W2-T06A` to `W2-T06C`, `W2-T07A`, `W2-T08A` to `W2-T08C` |
| Transfers and transfer history | `W2-T09A` to `W2-T09C` |
| Fixtures, match results, simulation | `W3-T01A` to `W3-T03C`, `W3-T08A` to `W3-T09B` |
| Scoring, team score updates, history | `W3-T04A` to `W3-T07B` |
| Chat, reports, moderation, notifications, messages | `W4-T01A` to `W4-T05B` |
| Admin search/reports, final QA, demo docs, API handoff | `W4-T06A` to `W4-T10B` |

## Out-of-Scope Unless Approved

These items appear in the broader requirement set or as possible enhancements, but should not block the MVP unless the project lead explicitly turns them into tickets:

- Real money payments, betting, gambling, official rugby data-provider integration, mobile apps, or professional analytics.
- Advanced AI recommendations beyond simple rule-based transfer suggestions.
- Full real-time infrastructure if a simple polling chat is accepted for the demo.
- Optional captain/vice-captain, badges, price changes, head-to-head fixtures, reactions, and watchlists.

---
# Week 1 — Backend Foundation and Architecture Correction

Week 1 focuses on producing a clean backend API foundation using `REST Resource → Service → DAO → Database`. Frontend WAR authentication can be prepared through `JSP/Page → Frontend Servlet → REST Client → Backend Resource`.

## [W1-BE01] Final Database Schema Design

**State:** UnassignedDefault  
**Labels:** database, backend, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Create the final MySQL database scripts for the project. This task sets up the tables, primary keys, foreign keys, constraints, and relationships that the backend DAO layer will use.

This is a database-layer task only. It does not create REST Resources, services, frontend pages, frontend servlets, or Java business logic.

**Simple task explanation:** Build the database structure that the whole backend will depend on.

**Why this matters:** This gives the project its tables and relationships, so later DAO code has a reliable place to save and read data.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `schema.sql`
- `drop_schema.sql`
- `seed.sql` as an empty/starter file for W1-BE04 to fill

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- ERD
- Class diagram
- Requirements specification
- `Model class list`

### Developer Notes

- The schema must support the features in scope: users, admins, players, clubs, positions, fantasy teams, leagues, fixtures, match stats, fantasy points, chat, reports, notifications, and private messages where included.
- Use SQL scripts for setup.
- Normal reset order is `drop_schema.sql → schema.sql → seed.sql → start backend → test API`.

### Architectural Responsibility Notes

- The database layer stores and protects the data structure.
- The DAO layer will later use this schema.
- No frontend or backend request handling belongs in this ticket.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** Requirements specification, ERD, class diagram, final agreed table names.
- **Unblocks:** W1-BE02, W1-BE04, all DAO tickets.
- **Handoff clarity:** Keep this as SQL/database work only. Any Java changes belong in later DAO/service/resource tickets.

### Acceptance Criteria

- [ ] `schema.sql` runs on a clean database.
- [ ] `drop_schema.sql` resets the database safely.
- [ ] `seed.sql` runs after the schema.
- [ ] Primary keys are correct.
- [ ] Foreign keys are correct.
- [ ] Relationships match the ERD/class design.
- [ ] The schema is ready for DAO work.

---

## [W1-BE02] Database Connection and DAO Base Setup

**State:** UnassignedDefault  
**Labels:** database, backend, mvp  
**Priority:** Urgent  
**Difficulty:** Medium

### Description

Create the shared backend database connection setup and base DAO structure. This allows all DAOs to connect to MySQL in one consistent way.

**Simple task explanation:** Build the shared database connection setup for the backend.

**Why this matters:** This stops every DAO from writing its own connection code and gives all database work one standard starting point.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `DBConnectionManager`
- `BaseDAO`
- `database.properties` or agreed database configuration file

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `pom.xml` to add the MySQL JDBC dependency

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `schema.sql`
- `seed.sql`
- JDBC driver
- `java.sql.Connection`
- `PreparedStatement`
- `ResultSet`

### Developer Notes

- All DAOs should use the shared connection setup.
- Do not let every DAO create its own separate connection code.
- Do not hardcode database credentials in many places.

### Architectural Responsibility Notes

- This is DAO/database infrastructure.
- DAO classes handle SQL only.
- Services use DAOs.
- REST Resources must not open database connections directly.
- Frontend servlets must never use this database connection class.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE01 at least mostly stable; MySQL driver dependency agreed.
- **Unblocks:** W1-BE08 and all feature DAO tasks.
- **Handoff clarity:** Create one shared connection path. Do not let feature developers duplicate connection code.

### Acceptance Criteria

- [ ] Backend can connect to MySQL.
- [ ] JDBC dependency works.
- [ ] `DBConnectionManager` returns usable connections.
- [ ] `BaseDAO` is available for shared DAO logic.
- [ ] DAO classes can reuse the setup.
- [ ] No feature business logic is added here.

---

## [W1-BE03] Create Backend Model Classes

**State:** UnassignedDefault  
**Labels:** backend, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Create the main backend model classes. Model classes represent system data such as users, players, clubs, leagues, fixtures, fantasy teams, messages, reports, and notifications.

**Simple task explanation:** Build the Java data objects used by the backend.

**Why this matters:** These classes give services, DAOs, and resources a shared way to represent users, players, teams, leagues, fixtures, messages, reports, and points.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `User`
- `RegisteredUser`
- `Administrator`
- `Club`
- `Player`
- `Position`
- `FantasyTeam`
- `FantasyTeamPlayer`
- `League`
- `LeagueMembership`
- `Fixture`
- `MatchResult`
- `PlayerMatchStatistic`
- `ScoringRule`
- `FantasyPointTransaction`
- `LeaderboardEntry`
- `ChatMessage`
- `PrivateMessage`
- `Report`
- `Notification`
- `UserRole`
- `FixtureStatus`
- `LeagueType`
- `ReportStatus`
- Other required enum classes agreed from the schema/class diagram

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `schema.sql`
- ERD
- Class diagram
- Requirements specification

### Developer Notes

- Models should mostly contain fields, constructors, getters, setters, and simple helper methods directly required by the model fields.
- Use enums instead of random strings for fixed values such as role, fixture status, league type, report status, and availability status.

### Architectural Responsibility Notes

- Models do not connect to the database.
- Models do not contain SQL.
- Models do not contain servlet logic.
- Models do not contain REST endpoint logic.
- Models should not contain large business rules.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE01 table/entity decisions and class diagram.
- **Unblocks:** W1-BE05, W1-BE06, W1-BE08, and most Week 2/3/4 service/DAO tasks.
- **Handoff clarity:** Models should compile without needing servlet, resource, or DAO code.

### Acceptance Criteria

- [ ] Model classes compile.
- [ ] Fields match the database design.
- [ ] Enums are used instead of random strings.
- [ ] Models do not contain SQL.
- [ ] Models do not contain frontend logic.
- [ ] Models do not contain API endpoint logic.

---

## [W1-BE04] Seed Data Script

**State:** UnassignedDefault  
**Labels:** database, backend, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create useful seed data for testing. Seed data allows developers to test login, admin access, player browsing, fantasy teams, leagues, fixtures, and later scoring without manually inserting records every time.

**Simple task explanation:** Fill the database with useful test data.

**Why this matters:** This lets the team test login, players, clubs, fixtures, leagues, and scoring without manually inserting records every time.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- None expected for this ticket.

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `seed.sql` created or stubbed by `W1-BE01`

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `schema.sql`
- `drop_schema.sql`
- ERD
- Class diagram

### Developer Notes

- Include one admin user, normal registered users, clubs, positions, players, fixtures, leagues, and basic scoring rules if the table exists.
- Passwords in seed data must use the same BCrypt hashing format used by `PasswordUtil` in the login system.
- Seed login accounts should document the test plain-text password in comments or project notes only; the `passwordHash` database field must contain the BCrypt hash string.

### Architectural Responsibility Notes

- This is a database setup task.
- Do not create Java seeders unless the project lead approves it.
- Do not create API endpoints here.
- Do not create frontend pages here.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE01 schema; BCrypt password hashing format from W1-BE05/W1-BE06 if login seed accounts are needed.
- **Unblocks:** Week 1 testing and demo data for Week 2/3 features.
- **Handoff clarity:** Keep seed data realistic but small enough to reset quickly.

### Acceptance Criteria

- [ ] `seed.sql` runs after `schema.sql`.
- [ ] Foreign key order is correct.
- [ ] Admin test account exists.
- [ ] Normal user test account exists.
- [ ] Players, clubs, positions, fixtures, and leagues are useful for testing.
- [ ] Seed passwords are stored as BCrypt hashes that work with `PasswordUtil` login verification.

---

## [W1-BE05] Registration Backend API

**State:** UnassignedDefault  
**Labels:** backend, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Create the backend registration API using a JAX-RS REST Resource. This replaces the incorrect idea of a backend registration servlet.

Correct backend flow: `AuthResource → AuthService / UserService → UserDAO / RegisteredUserDAO → Database`.

**Simple task explanation:** Build the backend API that registers new users.

**Why this matters:** This is the first real backend feature and proves the Resource → Service → DAO → Database flow works for account creation.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `AuthResource`
- `AuthService`
- `UserService`
- `UserDAO`
- `RegisteredUserDAO`
- `RegistrationRequest`
- `RegistrationResponse`
- `ApiResponse`
- `ErrorResponse`
- `PasswordUtil`
- `JsonUtil` or agreed JSON helper/provider wrapper
- `RestApplication` / JAX-RS application configuration class

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `pom.xml` to add required BCrypt password hashing and JSON dependencies

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `User`
- `RegisteredUser`
- `UserRole`
- `DBConnectionManager`
- `BaseDAO`
- BCrypt library/dependency used by `PasswordUtil`
- `JsonUtil or JSON provider`

### Developer Notes

- The backend REST Resource receives the API request and returns JSON.
- The Resource must call the service layer.
- The service validates registration rules.
- The service hashes the password with BCrypt through `PasswordUtil` before saving.
- `PasswordUtil` must wrap BCrypt hashing, for example using `BCrypt.hashpw(plainPassword, BCrypt.gensalt())` or the agreed BCrypt library equivalent.
- Store only the BCrypt hash string in the database `passwordHash` field. Do not store the plain password or a separate manually managed salt.
- The DAO inserts the user into the database.
- Basic rules: email required, username required, password required, email unique, username unique, password securely hashed with BCrypt, new users receive the normal registered user role.

### Architectural Responsibility Notes

- `AuthResource` must not contain SQL.
- `AuthResource` must not contain large business logic.
- `AuthService` / `UserService` contains validation and business rules.
- `UserDAO` / `RegisteredUserDAO` handles SQL only.
- Passwords and BCrypt hashes must never be returned in API responses.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE02, W1-BE03, W1-BE08 user DAO methods, PasswordUtil.
- **Unblocks:** W1-FE01 registration and protected-user workflows.
- **Handoff clarity:** Backend registration is a JAX-RS Resource, not a backend servlet.

### Acceptance Criteria

- [ ] Registration endpoint accepts JSON.
- [ ] Registration endpoint returns JSON.
- [ ] Required fields are validated.
- [ ] Duplicate email/username is handled.
- [ ] Password is hashed with BCrypt before saving.
- [ ] New user is saved correctly.
- [ ] No SQL exists inside the REST Resource.
- [ ] Password/BCrypt hash is not returned.

---

## [W1-BE06] Login, Logout, and Auth Status Backend API

**State:** UnassignedDefault  
**Labels:** backend, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Create the backend authentication API for login, logout, and auth status checking. This replaces the incorrect idea of backend login/logout servlets.

Correct backend flow: `AuthResource → AuthService → UserDAO → Database`.

**Simple task explanation:** Add login, logout, and auth status to the existing authentication backend.

**Why this matters:** This lets the frontend know who is logged in and gives later protected features a safe user identity to work with.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `LoginRequest`
- `LoginResponse`
- `AuthStatusResponse`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `AuthResource` to add login, logout, and auth-status endpoints
- `AuthService` to add login/logout/status logic
- `UserDAO` to add login lookup methods
- `ApiResponse` to use/standardise auth success responses
- `ErrorResponse` to use/standardise auth error responses
- `PasswordUtil` to expose password verification support

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `User`
- `RegisteredUser`
- `UserRole`
- Backend stateless auth/session-check mechanism
- `JSON provider`

### Developer Notes

- The backend Resource receives login/logout/auth status API requests.
- The service checks credentials.
- The DAO finds the user.
- Before later protected endpoints are built, the team must agree one auth approach and document it clearly. If the backend is truly stateless, the login flow must issue an auth token or equivalent signed identifier that the frontend sends on later requests. If the team instead uses server-side sessions, update the architecture wording and docs so the project is not incorrectly described as stateless.
- `PasswordUtil` verifies the password using BCrypt.
- Verification must use BCrypt's verification method, for example `BCrypt.checkpw(plainPassword, storedHash)`, not raw string comparison or manual re-hashing.
- Only safe user data may be returned: `userId`, `username`, `email`, and `role`.
- Never return password, BCrypt hash, or salt.

### Architectural Responsibility Notes

- The REST Resource returns JSON.
- Business rules stay in `AuthService`.
- SQL stays in `UserDAO`.
- Session/authentication must be handled safely according to the project requirements.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE02, W1-BE03, W1-BE08, PasswordUtil, registration data from W1-BE05 or seed data.
- **Unblocks:** W1-BE07, W1-FE01 login flow, and all logged-in user features.
- **Handoff clarity:** Keep auth status stateless from the backend API perspective. Do not expose password data.

### Acceptance Criteria

- [ ] Login endpoint accepts JSON.
- [ ] Correct login returns safe user details.
- [ ] Incorrect login returns a clear JSON error.
- [ ] Logout endpoint works.
- [ ] Session check endpoint works if required.
- [ ] The chosen auth mechanism is documented clearly enough for `AuthFilter`, frontend REST clients, and later protected endpoints to reuse consistently.
- [ ] Password verification uses `PasswordUtil` backed by BCrypt.
- [ ] Passwords/BCrypt hashes are never exposed.

---

## [W1-BE07] Role-Based Access Control Backend

**State:** UnassignedDefault  
**Labels:** backend, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Create backend role-based access control for protected API endpoints. This makes sure logged-out users and normal users cannot access restricted actions.

**Simple task explanation:** Protect backend endpoints using login and role checks.

**Why this matters:** This makes sure logged-out users, normal users, league managers, and admins can only do what they are allowed to do.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- JAX-RS authentication request filter, for example `AuthFilter`
- Role protection filter or helper, for example `RoleFilter`
- `RoleUtil`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `UserRole` to confirm required role values exist
- `ApiResponse` to standardise protected endpoint success responses
- `ErrorResponse` to support `401` and `403` JSON errors
- `RestApplication` / backend JAX-RS configuration to register filters

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `AuthResource`
- `AuthService`
- `User`
- `RegisteredUser`
- `Administrator`

### Developer Notes

- Protected API endpoints must block logged-out users.
- Admin API endpoints must block normal users.
- League manager actions must later check league manager permissions.
- Blocked requests must return JSON, not JSP pages.
- Example responses: `401 — You must be logged in`, `403 — Admin access required`.

### Architectural Responsibility Notes

- This protects backend REST Resources.
- Role checks may be handled by filters/helpers and service-level checks.
- Frontend route guards are useful for user experience, but backend permission checks are final.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE06 auth mechanism and UserRole enum from W1-BE03.
- **Unblocks:** All admin/protected endpoints in Weeks 2 to 4.
- **Handoff clarity:** Frontend checks are helpful, but backend filters/service checks are final.

### Acceptance Criteria

- [ ] Logged-out users are blocked from protected endpoints.
- [ ] Normal users are blocked from admin endpoints.
- [ ] Admin users can access admin endpoints.
- [ ] JSON error responses are returned.
- [ ] Role names use `UserRole`.
- [ ] Protected routes are tested.

---

## [W1-BE08] User Account DAO and Service Methods

**State:** UnassignedDefault  
**Labels:** backend, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Create reusable DAO and service methods for user account features. These methods support registration, login, profile updates, duplicate checks, and later admin user management.

**Simple task explanation:** Add reusable user account methods that other features can call.

**Why this matters:** This prevents duplicate user lookup and account-update code from being copied into every later feature.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `AdministratorDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `UserDAO` to add reusable user lookup/create/update methods
- `RegisteredUserDAO` to add registered-user specific methods
- `UserService` to add reusable account rules
- `AuthService` to reuse the new DAO/service methods instead of duplicate logic

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `User`
- `RegisteredUser`
- `Administrator`
- `UserRole`
- `DBConnectionManager`
- `BaseDAO`
- `PasswordUtil`

### Developer Notes

- DAO examples: find user by ID, find user by email, find user by username, create user, update user, check if email exists, check if username exists.
- Service examples: validate registration, validate profile updates, decide whether account changes are allowed, coordinate BCrypt password hashing for registration, login, and password-change flows through `PasswordUtil`.

### Architectural Responsibility Notes

- DAO handles SQL only.
- Service handles business rules.
- REST Resource handles API request/response only.
- Frontend must not call DAOs.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE02 and W1-BE03.
- **Unblocks:** W1-BE05, W1-BE06, profile/admin user management later.
- **Handoff clarity:** Put reusable user SQL here so registration/login do not duplicate it.

### Acceptance Criteria

- [ ] User lookup by ID works.
- [ ] User lookup by email/username works.
- [ ] User creation works.
- [ ] Duplicate checks work.
- [ ] User update support exists if required.
- [ ] SQL is only in DAOs.
- [ ] Services contain user account rules.

---

## [W1-BE09] Backend API Smoke Test Endpoints

**State:** UnassignedDefault  
**Labels:** backend, integration, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create simple backend API smoke tests so the team can quickly confirm the backend foundation works. This replaces the incorrect wording “backend test servlets”.

**Simple task explanation:** Create small backend test endpoints for checking that the backend is alive and protected.

**Why this matters:** These endpoints help the team quickly prove that GlassFish, the API, the database, and role checks are working.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `HealthResource`
- `DatabaseHealthResource`
- `ProtectedTestResource`
- `AdminTestResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `ApiResponse` to use/standardise smoke-test success responses
- `ErrorResponse` to use/standardise smoke-test error responses
- `RestApplication` / backend JAX-RS configuration to expose test resources

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `DBConnectionManager`
- `AuthService`
- `UserService`
- `AuthFilter`
- `RoleFilter`
- `UserDAO`

### Developer Notes

- These are not production features.
- They are simple JSON endpoints for checking backend running, database connection, protected routes, and admin routes.
- Example endpoints: `/api/test/health`, `/api/test/database`, `/api/test/protected`, `/api/test/admin`.

### Architectural Responsibility Notes

- These are backend REST Resource smoke tests.
- They return JSON.
- They must not become the real feature implementation.
- They must not contain large business logic.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE02, W1-BE06, W1-BE07.
- **Unblocks:** Week 1 verification and quick architecture checks for the team.
- **Handoff clarity:** These endpoints are only smoke tests; do not build real features inside them.

### Acceptance Criteria

- [ ] Health endpoint returns JSON.
- [ ] Database health endpoint confirms DB connection.
- [ ] Protected test endpoint blocks logged-out users.
- [ ] Admin test endpoint blocks normal users.
- [ ] Test endpoints are clearly marked as test/smoke endpoints.

---

## [W1-BE10] Backend Foundation Testing Checklist

**State:** UnassignedDefault  
**Labels:** documentation, integration, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create and complete the Week 1 backend testing checklist. This confirms that the database, models, DAOs, REST Resources, services, registration, login, sessions, and role checks work together.

**Simple task explanation:** Write and complete the Week 1 backend testing checklist.

**Why this matters:** This makes the team verify the foundation before Week 2 features depend on it.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `week-1-backend-checklist.md`
- Week 1 GitHub bug notes/issues for anything that fails

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `TESTING.md` to include Week 1 testing steps and results

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `schema.sql`
- `drop_schema.sql`
- `seed.sql`
- `DBConnectionManager`
- `UserDAO`
- `UserService`
- `AuthService`
- `AuthResource`
- `HealthResource`
- `AuthFilter`
- `RoleFilter`

### Developer Notes

- Test in this order: reset database → run `schema.sql` → run `seed.sql` → start backend WAR → test health endpoint → test database endpoint → test registration → test login → test logout/auth status → test protected endpoint → test admin endpoint.

### Architectural Responsibility Notes

- This is a verification task.
- Do not add major new features here.
- Record bugs clearly.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE01 to W1-BE09 and W1-FE01 if frontend auth is included in Week 1.
- **Unblocks:** Week 2 implementation confidence.
- **Handoff clarity:** Record bugs as separate issues instead of hiding them inside the checklist.

### Acceptance Criteria

- [ ] Database reset works.
- [ ] Backend starts on GlassFish.
- [ ] REST Resources are reachable.
- [ ] Database connection works.
- [ ] Registration works.
- [ ] Login works.
- [ ] Logout/auth status check works if required.
- [ ] Protected endpoints block logged-out users.
- [ ] Admin endpoints block normal users.
- [ ] All Week 1 bugs are recorded clearly.

---

## [W1-BE11] Profile and Password Management Backend API

**State:** UnassignedDefault  
**Labels:** backend, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create the backend API for a logged-in user to view/update their own profile details and change their password safely.

**Simple task explanation:** Build the backend account-management API for profile updates and password changes.

**Why this matters:** The task plan already claims profile/security foundation as part of scope. This ticket closes that gap so the final project can demonstrate more than registration and login.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `ProfileResource` or agreed `AccountResource`
- `ProfileResponse`
- `ProfileUpdateRequest`
- `ChangePasswordRequest`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `UserService` to add profile-update and password-change rules
- `UserDAO` to add profile read/update methods
- `RegisteredUserDAO` if registered-user table details are stored separately
- `PasswordUtil` only if helper methods are needed for password-change verification/hashing

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket. Wait for them, pull the latest branch, import them, call them, or coordinate with the owner ticket.

- `AuthFilter`
- `User`
- `RegisteredUser`
- `UserRole`
- `UserService`
- `UserDAO`
- `PasswordUtil`

### Developer Notes

- Users may only view or edit their own profile unless a separate admin account-management feature is explicitly approved.
- Password change must require the current password unless the project lead approves an admin-reset flow.
- Profile responses must return safe fields only. Never return password, BCrypt hash, or internal security data.

### Architectural Responsibility Notes

- Resource handles JSON request/response only.
- Service validates ownership, uniqueness, and password rules.
- DAO performs reads/updates.
- Password hashing and verification must still go through `PasswordUtil`.


### TICKET DEPENDENCIES AND HANDOFF

Use this section to know what must be completed first and what this ticket unlocks for the next developer.

- **Depends on:** W1-BE05, W1-BE06, W1-BE07, W1-BE08.
- **Unblocks:** Final account-management scope, clearer demo accounts, and accurate requirement traceability.
- **Handoff clarity:** Keep this user-self-service only unless an admin-management requirement is separately defined.

### Acceptance Criteria

- [ ] Logged-in user can view own profile details.
- [ ] Logged-in user can update allowed profile fields.
- [ ] Logged-in user can change password using current-password verification.
- [ ] Duplicate email/username rules still apply where relevant.
- [ ] Password changes are hashed through `PasswordUtil`.
- [ ] Users cannot update another user's profile.
- [ ] Passwords/BCrypt hashes are never returned.

---

# Week 1 Urgent Frontend Correction Tasks

Because the project uses a separated frontend WAR and backend WAR, the frontend authentication flow should be completed after the Week 1 backend auth foundation. Week 1 backend tickets above are completed historical work and are not restructured in this pass.

## [W1-FE01A] Frontend Auth REST Client and API Config

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, auth, mvp  
**Priority:** Urgent  
**Difficulty:** Medium

### Description

Create the frontend API client support for register, login, logout, and auth-status calls. This ticket owns HTTP client wiring only, not JSP pages or backend validation.

**Simple task explanation:** Complete the focused work for Frontend Auth REST Client and API Config.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `ApiClient`
- `AuthRestClient`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Frontend API base URL configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Backend AuthResource` — owned by `W1-BE05/W1-BE06`; wait for endpoint contract or use agreed contract because request paths and payloads must match.
- `ApiResponse/ErrorResponse format` — owned by `W1-BE05/W1-BE06`; can work from agreed fields because frontend errors must display consistently.

### Developer Notes

- Keep password hashing on the backend through `PasswordUtil`.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W1-FE01B] Register and Login JSP Pages

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, auth, mvp  
**Priority:** Urgent  
**Difficulty:** Easy

### Description

Create the browser pages for registration and login. Pages collect user input and post to the frontend servlet; they do not call DAOs or hash passwords.

**Simple task explanation:** Complete the focused work for Register and Login JSP Pages.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `register.jsp`
- `login.jsp`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared frontend layout/navigation to add login/register links

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `AuthServlet route names` — owned by `W1-FE01C`; can work from agreed form actions because forms must post to the correct frontend servlet.
- `Backend auth errors` — owned by `W1-BE05/W1-BE06`; can work from agreed message fields because pages must show validation failures clearly.

### Developer Notes

- Use only lightweight client-side required-field hints.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W1-FE01C] Auth Servlet and Frontend Session Flow

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, auth, mvp  
**Priority:** Urgent  
**Difficulty:** Medium

### Description

Create the frontend servlet flow that receives auth form posts, calls `AuthRestClient`, stores only safe frontend session/auth data, and redirects users.

**Simple task explanation:** Complete the focused work for Auth Servlet and Frontend Session Flow.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `AuthServlet`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Frontend web config/routing
- Shared navigation/session display if needed

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `AuthRestClient` — owned by `W1-FE01A`; wait for method signatures or agreed interface because the servlet must not duplicate HTTP client logic.
- `register.jsp/login.jsp` — owned by `W1-FE01B`; can work from agreed field names because form parameters must match.
- `Backend login response` — owned by `W1-BE06`; wait for final auth contract before full testing because session/token fields must be stored correctly.

### Developer Notes

- Store only safe fields such as user id, username, role, and token/cookie reference.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

---
# Week 2 — Fantasy Team and League Foundation

Week 2 focuses on gameplay foundation. Backend tasks must follow `Resource -> Service -> DAO -> Database`. Frontend tasks must follow `JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource`.

## [W2-T01A] Player, Club, and Position DTO Contract

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dto, mvp  
**Priority:** High  
**Difficulty:** Easy

### Description

Define backend request/response DTOs for players, clubs, and positions so DAO, service, Resource, and frontend work can use one payload contract.

**Simple task explanation:** Complete the focused work for Player, Club, and Position DTO Contract.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Player request/response DTOs
- Club request/response DTOs
- Position request/response DTOs

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `API_CONTRACT.md` if it already exists and should list these payloads

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Player/Club/Position models` — owned by `W1-BE03`; wait for compile-ready fields because DTOs must map to real backend models.
- `ErrorResponse format` — owned by `W1-BE05`; can work from agreed fields because validation errors must be consistent.

### Developer Notes

- Include ids, names, club/position references, value, and availability/status fields as needed.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DTOs define API payloads and must not contain SQL or business rules.
- Resources map DTOs to service calls.
- Frontend consumes DTO shapes through REST clients.

### Acceptance Criteria

- [ ] Payload classes or documented shapes are clear enough for dependent work.
- [ ] No SQL or business rules exist in DTOs.
- [ ] Fields match the agreed model/API contract.
- [ ] Dependencies can use the contract without recreating it.
- [ ] The ticket is ready for the next dependent work.

## [W2-T01B] Club and Position DAO Mapping

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, database, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create DAO methods for club and position lookup, list, create, update, duplicate checks, and deactivate/status operations.

**Simple task explanation:** Complete the focused work for Club and Position DAO Mapping.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `ClubDAO`
- `PositionDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `schema.sql` — owned by `W1-BE01`; wait for final table/column names because SQL must match the database.
- `DBConnectionManager/BaseDAO` — owned by `W1-BE02`; wait for shared connection setup because DAOs must use the standard connection path.
- `Club/Position models` — owned by `W1-BE03`; wait for models because row mapping returns model objects.

### Developer Notes

- Use prepared statements only.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W2-T01C] Player DAO Mapping

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, database, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create DAO methods for player lookup, list/search filters, create, update, deactivate, availability, club, and position queries.

**Simple task explanation:** Complete the focused work for Player DAO Mapping.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `PlayerDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `schema.sql` — owned by `W1-BE01`; wait for final player columns because SQL must match player table design.
- `DBConnectionManager/BaseDAO` — owned by `W1-BE02`; wait for shared connection setup because DAO must use the standard connection path.
- `ClubDAO/PositionDAO` — owned by `W2-T01B`; can work from agreed foreign-key fields because players reference club and position records.

### Developer Notes

- Do not calculate fantasy budget or team validity here.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W2-T01D] Admin Player, Club, and Position Services

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create service-layer validation and business rules for maintaining player, club, and position records.

**Simple task explanation:** Complete the focused work for Admin Player, Club, and Position Services.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `PlayerService`
- `ClubService`
- `PositionService`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Player/Club/Position DTOs` — owned by `W2-T01A`; can work from agreed fields because service methods need request/response shapes.
- `ClubDAO/PositionDAO` — owned by `W2-T01B`; wait for method signatures or agreed interfaces because services need lookup and duplicate checks.
- `PlayerDAO` — owned by `W2-T01C`; wait for method signatures or agreed interfaces because player validation uses player persistence.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; can work from agreed role names because admin-only operations require backend protection.

### Developer Notes

- Reject negative player values and missing club/position references.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W2-T01E] Admin Player, Club, and Position Resources

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create backend JAX-RS Resources for admin maintenance plus safe player/club/position read/search endpoints.

**Simple task explanation:** Complete the focused work for Admin Player, Club, and Position Resources.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `PlayerResource`
- `ClubResource`
- `PositionResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Player/Club/Position services` — owned by `W2-T01D`; wait for service methods because Resources must delegate validation and persistence.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; wait before protected endpoint testing because admin create/update/deactivate endpoints need role protection.
- `ApiResponse/ErrorResponse` — owned by `W1-BE05`; can work from agreed format because JSON responses must be consistent.

### Developer Notes

- Read/search routes may be public or logged-in depending on final route rules.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Resources accept HTTP/JSON requests, call services, and return JSON responses.
- Resources must not contain SQL or large business logic.
- Backend filters/services remain final permission authority.

### Acceptance Criteria

- [ ] Endpoint accepts the required request format.
- [ ] Endpoint returns the required JSON response format.
- [ ] Protected actions use backend auth/role checks.
- [ ] Resource contains no SQL or large business logic.
- [ ] Errors and empty states are handled clearly.

## [W2-T02A] Player and Club Frontend REST Clients and Servlets

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create frontend REST clients and servlet routes for player and club browsing. This ticket does not build final JSP layout.

**Simple task explanation:** Complete the focused work for Player and Club Frontend REST Clients and Servlets.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `PlayerRestClient`
- `ClubRestClient`
- `PlayerServlet`
- `ClubServlet`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Frontend API base URL configuration if needed

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `PlayerResource/ClubResource` — owned by `W2-T01E`; wait for endpoints or use agreed contract because frontend must request backend data.
- `Player/Club DTOs` — owned by `W2-T01A`; can work from agreed fields because frontend parsing and request attributes need known fields.
- `Frontend auth session flow` — owned by `W1-FE01C`; can work from agreed session fields because navigation may depend on login state.

### Developer Notes

- Servlets prepare view data and forward to JSP pages.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W2-T02B] Player and Club Browsing JSP Pages

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** High  
**Difficulty:** Easy

### Description

Create JSP pages for viewing/searching players and clubs using data provided by frontend servlets.

**Simple task explanation:** Complete the focused work for Player and Club Browsing JSP Pages.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `players.jsp`
- `clubs.jsp`
- Small search/filter JavaScript helper if needed

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add Players and Clubs links

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `PlayerServlet/ClubServlet` — owned by `W2-T02A`; can work from agreed request attributes because JSP pages need data and errors supplied by servlets.
- `Player/Club DTO display fields` — owned by `W2-T01A`; can work from agreed fields because tables need names, clubs, positions, values, and availability.

### Developer Notes

- Show empty states for no players or clubs.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W2-T03A] Fantasy Team DTO Contract

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dto, mvp  
**Priority:** High  
**Difficulty:** Easy

### Description

Define request/response DTOs for fantasy team creation, viewing, updating, selected squad players, budget summary, and validation errors.

**Simple task explanation:** Complete the focused work for Fantasy Team DTO Contract.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Fantasy team request DTOs
- Fantasy team response DTOs
- Fantasy team player selection DTOs

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `API_CONTRACT.md` if it already exists and should list fantasy team payloads

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FantasyTeam/FantasyTeamPlayer models` — owned by `W1-BE03`; wait for model fields because DTOs must map to real backend models.
- `Player response DTOs` — owned by `W2-T01A`; can work from agreed fields because team responses may include selected player summaries.

### Developer Notes

- Do not trust frontend-calculated total value as final data.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DTOs define API payloads and must not contain SQL or business rules.
- Resources map DTOs to service calls.
- Frontend consumes DTO shapes through REST clients.

### Acceptance Criteria

- [ ] Payload classes or documented shapes are clear enough for dependent work.
- [ ] No SQL or business rules exist in DTOs.
- [ ] Fields match the agreed model/API contract.
- [ ] Dependencies can use the contract without recreating it.
- [ ] The ticket is ready for the next dependent work.

## [W2-T03B] Fantasy Team DAO and Squad Persistence

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, database, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create DAO support for fantasy teams and selected squad players, including create, load, replace squad, and update score/total fields needed later.

**Simple task explanation:** Complete the focused work for Fantasy Team DAO and Squad Persistence.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `FantasyTeamDAO`
- `FantasyTeamPlayerDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `schema.sql` — owned by `W1-BE01`; wait for fantasy team tables because DAO SQL must match the database.
- `DBConnectionManager/BaseDAO` — owned by `W1-BE02`; wait for shared connection setup because DAOs must use the standard connection path.
- `FantasyTeam/FantasyTeamPlayer models` — owned by `W1-BE03`; wait for models because row mapping returns model objects.
- `PlayerDAO` — owned by `W2-T01C`; can work from agreed player id contract because squad rows reference players.

### Developer Notes

- Use transactions for multi-row team and squad saves.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W2-T04A] Reusable Squad Validation Service

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create reusable squad validation for team creation and transfers: squad size, position rules, budget, duplicates, invalid ids, and player availability.

**Simple task explanation:** Complete the focused work for Reusable Squad Validation Service.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `SquadValidationService`
- `SquadValidationResult`
- `SquadValidationError`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `PlayerDAO` — owned by `W2-T01C`; wait for lookup methods because validation needs value, position, club, and availability.
- `FantasyTeamDAO/FantasyTeamPlayerDAO` — owned by `W2-T03B`; can work from agreed methods because validation may need existing team/squad state.
- `Fantasy team DTOs` — owned by `W2-T03A`; can work from selected-player fields because validation receives selected player ids.

### Developer Notes

- Return clear validation errors that Resources and frontend can display.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W2-T03C] Fantasy Team Service

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create service-layer logic for creating, viewing, and updating the logged-in user fantasy team with ownership checks, budget calculation, and squad validation.

**Simple task explanation:** Complete the focused work for Fantasy Team Service.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `FantasyTeamService`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Fantasy team DTOs` — owned by `W2-T03A`; can work from agreed fields because service methods need payload shapes.
- `FantasyTeamDAO/FantasyTeamPlayerDAO` — owned by `W2-T03B`; wait for DAO methods because service saves and loads teams.
- `SquadValidationService` — owned by `W2-T04A`; wait for validation contract because invalid squads must not be saved.
- `AuthFilter user identity` — owned by `W1-BE07`; can work from agreed current-user access because ownership checks require current user id.

### Developer Notes

- Backend calculates total value and remaining budget.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W2-T03D] Fantasy Team Resource

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create protected JAX-RS endpoints for fantasy team creation, viewing, and update where included in MVP.

**Simple task explanation:** Complete the focused work for Fantasy Team Resource.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `FantasyTeamResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FantasyTeamService` — owned by `W2-T03C`; wait for service methods because Resource must delegate ownership, validation, and persistence.
- `Fantasy team DTOs` — owned by `W2-T03A`; can work from agreed fields because Resource request/response mapping needs DTOs.
- `AuthFilter` — owned by `W1-BE07`; wait before protected endpoint testing because team endpoints require logged-in identity.

### Developer Notes

- Return backend validation errors clearly.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Resources accept HTTP/JSON requests, call services, and return JSON responses.
- Resources must not contain SQL or large business logic.
- Backend filters/services remain final permission authority.

### Acceptance Criteria

- [ ] Endpoint accepts the required request format.
- [ ] Endpoint returns the required JSON response format.
- [ ] Protected actions use backend auth/role checks.
- [ ] Resource contains no SQL or large business logic.
- [ ] Errors and empty states are handled clearly.

## [W2-T05A] Fantasy Team Frontend REST Client and Servlet

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create the frontend REST client and servlet for loading player options and submitting fantasy team creation requests to the backend.

**Simple task explanation:** Complete the focused work for Fantasy Team Frontend REST Client and Servlet.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `FantasyTeamRestClient`
- `FantasyTeamServlet`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Frontend auth/session guard for create-team access

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FantasyTeamResource` — owned by `W2-T03D`; wait for endpoint contract because frontend submission must match backend payload and auth requirements.
- `PlayerRestClient` — owned by `W2-T02A`; reuse or wait for list/search methods because team creation needs selectable player data.
- `Fantasy team DTOs` — owned by `W2-T03A`; can work from agreed fields because frontend must serialize selected players correctly.

### Developer Notes

- Do not duplicate final squad validation in the servlet.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W2-T05B] Fantasy Team Creation JSP and Selection UI

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create the create-team page where a user searches/selects players, sees a budget preview, enters a team name, and submits to the frontend servlet.

**Simple task explanation:** Complete the focused work for Fantasy Team Creation JSP and Selection UI.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `create-team.jsp`
- Player selection UI
- Budget display UI
- Small JavaScript selection preview if needed

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add Create Team link

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FantasyTeamServlet` — owned by `W2-T05A`; can work from agreed request attributes and form fields because the page needs data, errors, and submit route.
- `Player DTOs` — owned by `W2-T01A`; can work from display fields because the page displays names, clubs, positions, values, and availability.
- `SquadValidationService errors` — owned by `W2-T04A`; can work from agreed error keys/messages because backend validation failures must display clearly.

### Developer Notes

- Budget preview is UX only; backend calculates final totals.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W2-T06A] League DAO and DTO Foundation

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, database, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create league/membership DTOs and DAO methods for creating leagues, loading leagues, checking join codes, and reading memberships.

**Simple task explanation:** Complete the focused work for League DAO and DTO Foundation.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- League request/response DTOs
- `LeagueDAO`
- `LeagueMembershipDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `schema.sql` — owned by `W1-BE01`; wait for league and membership tables because DAO SQL must match table names and keys.
- `League/LeagueMembership models` — owned by `W1-BE03`; wait for model fields because mapping and DTOs use these fields.
- `DBConnectionManager/BaseDAO` — owned by `W1-BE02`; wait for shared DB setup because DAOs must use the standard connection path.

### Developer Notes

- Keep private join codes out of public responses unless explicitly needed.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W2-T06B] League Service and Membership Rules

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create service-layer rules for league creation, private league visibility, league-manager assignment, and membership checks.

**Simple task explanation:** Complete the focused work for League Service and Membership Rules.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `LeagueService`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `League DAOs/DTOs` — owned by `W2-T06A`; wait for methods or agreed interfaces because service persists leagues and memberships.
- `FantasyTeamDAO` — owned by `W2-T03B`; can work from agreed lookup method because project rules may require a fantasy team before league actions.
- `AuthFilter user identity` — owned by `W1-BE07`; can work from agreed current-user access because league actions depend on logged-in user.

### Developer Notes

- Creator of a private league should become league manager if that is the agreed rule.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W2-T06C] League Resource

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create backend Resource endpoints for league creation, viewing, and listing while delegating visibility and permission rules to `LeagueService`.

**Simple task explanation:** Complete the focused work for League Resource.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `LeagueResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `LeagueService` — owned by `W2-T06B`; wait for service methods because Resource must delegate league rules and visibility checks.
- `League DTOs` — owned by `W2-T06A`; can work from agreed fields because request/response mapping needs payloads.
- `AuthFilter` — owned by `W1-BE07`; wait before protected testing because private league creation and visibility require authenticated users.

### Developer Notes

- Do not expose private join codes to non-managers unless required.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Resources accept HTTP/JSON requests, call services, and return JSON responses.
- Resources must not contain SQL or large business logic.
- Backend filters/services remain final permission authority.

### Acceptance Criteria

- [ ] Endpoint accepts the required request format.
- [ ] Endpoint returns the required JSON response format.
- [ ] Protected actions use backend auth/role checks.
- [ ] Resource contains no SQL or large business logic.
- [ ] Errors and empty states are handled clearly.

## [W2-T07A] Join Public and Private Leagues Backend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Add backend support for joining public and private leagues, including join DTOs, service rules, Resource endpoints, duplicate checks, and valid-code checks.

**Simple task explanation:** Complete the focused work for Join Public and Private Leagues Backend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Join league request DTO
- Join league response DTO

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `LeagueResource` to add join endpoints
- `LeagueService` to add joining rules
- `LeagueDAO`/`LeagueMembershipDAO` for missing join lookups

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `League foundation` — owned by `W2-T06A/W2-T06B/W2-T06C`; wait for league persistence and Resource structure because join logic extends leagues and memberships.
- `FantasyTeamDAO` — owned by `W2-T03B`; can work from agreed lookup method because joining may require an existing fantasy team.
- `AuthFilter` — owned by `W1-BE07`; wait before protected testing because joining requires logged-in user identity.

### Developer Notes

- Reject duplicate membership and invalid private codes consistently.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W2-T08A] Leaderboard DAO and Service

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, service, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create backend DAO/service support for reading league leaderboard rows. Week 3 owns score updates; this ticket owns safe reads and initial ranking data.

**Simple task explanation:** Complete the focused work for Leaderboard DAO and Service.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `LeaderboardDAO`
- `LeaderboardService`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `LeagueMembershipDAO` — owned by `W2-T06A`; wait for membership lookup methods because private leaderboard access depends on membership checks.
- `FantasyTeamDAO` — owned by `W2-T03B`; wait for team total fields or agreed query because leaderboard rows are based on fantasy teams.
- `LeaderboardEntry model` — owned by `W1-BE03`; wait for model fields because ranking fields need a model/DTO target.

### Developer Notes

- Show rank, team name, manager name, weekly points, and total points if available.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W2-T08B] Leaderboard Resource

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, resource, mvp  
**Priority:** High  
**Difficulty:** Easy

### Description

Create the backend endpoint for requesting league leaderboard data and delegate ranking/permission work to `LeaderboardService`.

**Simple task explanation:** Complete the focused work for Leaderboard Resource.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `LeaderboardResource`
- Leaderboard response DTOs if not already created

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `LeaderboardService` — owned by `W2-T08A`; wait for service method because Resource must delegate rank loading and private league checks.
- `AuthFilter` — owned by `W1-BE07`; wait before private-league testing because private leaderboards need current user identity.
- `ApiResponse/ErrorResponse` — owned by `W1-BE05`; can work from agreed format because responses should match backend standard.

### Developer Notes

- Return clear not-found, forbidden, and empty leaderboard responses.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Resources accept HTTP/JSON requests, call services, and return JSON responses.
- Resources must not contain SQL or large business logic.
- Backend filters/services remain final permission authority.

### Acceptance Criteria

- [ ] Endpoint accepts the required request format.
- [ ] Endpoint returns the required JSON response format.
- [ ] Protected actions use backend auth/role checks.
- [ ] Resource contains no SQL or large business logic.
- [ ] Errors and empty states are handled clearly.

## [W2-T08C] Leaderboard Frontend Page and Client

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Create the frontend REST client, servlet, and JSP page for displaying league rankings returned by the backend.

**Simple task explanation:** Complete the focused work for Leaderboard Frontend Page and Client.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `LeaderboardRestClient`
- `LeaderboardServlet`
- `leaderboard.jsp`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add Leaderboard link

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `LeaderboardResource` — owned by `W2-T08B`; wait for endpoint contract because frontend must request ranking rows from backend.
- `Frontend auth/session flow` — owned by `W1-FE01C`; can work from session fields because private leaderboard pages may need logged-in state.
- `Leaderboard DTOs` — owned by `W2-T08B`; can work from agreed fields because page needs rank, team, manager, and point fields.

### Developer Notes

- Handle empty leaderboards and forbidden private-league responses.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W2-T09A] Transfer DTOs and History DAO

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, dto, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create transfer request/response DTOs and DAO support for recording transfer history.

**Simple task explanation:** Complete the focused work for Transfer DTOs and History DAO.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Transfer request DTOs
- Transfer response DTOs
- `TransferDAO`
- `TransferHistoryDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `schema.sql` — owned by `W1-BE01`; wait for transfer/history tables because DAO SQL must match persistence design.
- `FantasyTeam/FantasyTeamPlayer models` — owned by `W1-BE03`; wait for model fields because transfers reference teams and selected players.
- `Player model` — owned by `W1-BE03`; wait for model fields because transfer records reference players in and out.

### Developer Notes

- Record player out, player in, team, user, timestamp, and resulting values if required.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W2-T09B] Transfer Service and Lock Rules

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create service-layer transfer rules: ownership, incoming/outgoing player checks, budget, squad validation reuse, history recording, and agreed lock-window behavior.

**Simple task explanation:** Complete the focused work for Transfer Service and Lock Rules.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `TransferService`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `SquadValidationService` only if transfer support is missing
- `FantasyTeamDAO`/`FantasyTeamPlayerDAO` for missing transfer update methods

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Transfer DTOs/history DAO` — owned by `W2-T09A`; wait for DTOs and history persistence because service needs payloads and audit storage.
- `FantasyTeam service/DAOs` — owned by `W2-T03B/W2-T03C`; wait for team lookup and ownership support because transfers modify existing teams.
- `SquadValidationService` — owned by `W2-T04A`; wait for reusable validation because post-transfer squads must remain valid.
- `PlayerDAO` — owned by `W2-T01C`; wait for player lookup because service needs incoming player value, position, and availability.
- `FixtureStatus/lock rule` — owned by `W3-T01A or project lead`; can work from agreed contract because transfer windows must align with fixture status before scoring.

### Developer Notes

- Save transfer history only when the transfer succeeds.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W2-T09C] Transfer Resource

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create protected backend endpoints for transfer actions and transfer history reads.

**Simple task explanation:** Complete the focused work for Transfer Resource.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `TransferResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `TransferService` — owned by `W2-T09B`; wait for service methods because Resource must delegate validation, ownership, and persistence.
- `Transfer DTOs` — owned by `W2-T09A`; can work from agreed fields because request/response mapping needs payloads.
- `AuthFilter` — owned by `W1-BE07`; wait before protected testing because transfers require current user identity.

### Developer Notes

- Return validation errors from the service clearly.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Resources accept HTTP/JSON requests, call services, and return JSON responses.
- Resources must not contain SQL or large business logic.
- Backend filters/services remain final permission authority.

### Acceptance Criteria

- [ ] Endpoint accepts the required request format.
- [ ] Endpoint returns the required JSON response format.
- [ ] Protected actions use backend auth/role checks.
- [ ] Resource contains no SQL or large business logic.
- [ ] Errors and empty states are handled clearly.

## [W2-T10] Week 2 Feature Testing Checklist

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** integration, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create and complete the Week 2 testing checklist covering player admin, browsing, fantasy team creation, validation, leagues, leaderboards, and transfers.

**Simple task explanation:** Complete the focused work for Week 2 Feature Testing Checklist.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `week-2-testing-checklist.md`
- Week 2 GitHub bug notes/issues for anything that fails

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `TESTING.md` to add Week 2 feature test results

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Week 2 backend tickets` — owned by `W2-T01A through W2-T09C`; wait for implemented features before final checklist completion because the checklist must verify actual integrated behavior.
- `Week 2 frontend pages` — owned by `W2-T02A/W2-T02B/W2-T05A/W2-T05B/W2-T08C`; wait for pages before frontend checks because full flow testing includes frontend behavior.
- `Auth foundation` — owned by `W1-BE07 and W1-FE01C`; wait for protected flow because permissions and logged-in flows must be tested.

### Developer Notes

- Test logged-out users, normal users, admin users, invalid data, duplicates, private league access, ownership actions, and frontend API errors.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Testing should verify the full frontend-to-backend-to-database flow where applicable.
- Record bugs clearly instead of hiding them inside broad tickets.
- Do not add unrelated feature scope during verification.

### Acceptance Criteria

- [ ] Checklist or test evidence covers the named features.
- [ ] Permission and ownership cases are tested where relevant.
- [ ] Errors or bugs are recorded clearly.
- [ ] Correct layer responsibility is verified.
- [ ] The ticket is ready for the next dependent work.

---
# Week 3 — Match Processing and Fantasy Scoring

Week 3 proves the fantasy scoring system works. Admins enter match data or run simulation, but admins must not manually edit fantasy points.

## [W3-T01A] Fixture DTO and DAO Foundation

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, dto, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create fixture DTOs and DAO methods for fixture create, update, status, and lookup operations.

**Simple task explanation:** Complete the focused work for Fixture DTO and DAO Foundation.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Fixture request/response DTOs
- `FixtureDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `FixtureStatus` only if required statuses are missing

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Fixture model` — owned by `W1-BE03`; wait for model fields because DTOs and DAO mapping depend on the model.
- `ClubDAO` — owned by `W2-T01B`; wait for club lookup because fixtures reference home and away clubs.
- `schema.sql` — owned by `W1-BE01`; wait for fixture table fields because DAO SQL must match the database.

### Developer Notes

- Map statuses consistently between SQL, enum, and DTOs.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W3-T01B] Fixture Service and Resource

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create fixture service validation and backend Resource endpoints for fixture create, update, status changes, and viewing.

**Simple task explanation:** Complete the focused work for Fixture Service and Resource.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `FixtureService`
- `FixtureResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Fixture DTO/DAO` — owned by `W3-T01A`; wait for methods and payloads because service and Resource depend on fixture persistence and DTOs.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; wait before admin testing because fixture create/update endpoints require admin protection.
- `ClubDAO` — owned by `W2-T01B`; wait for lookup methods because service must reject invalid clubs and same-club fixtures.

### Developer Notes

- Reject fixtures where home and away clubs are the same.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T02A] Fixture Frontend Pages and Client

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Create frontend support for viewing fixtures and fixture details/results using backend fixture APIs.

**Simple task explanation:** Complete the focused work for Fixture Frontend Pages and Client.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `FixtureRestClient`
- `FixtureServlet`
- `fixtures.jsp`
- `fixture-details.jsp`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add Fixtures link

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FixtureResource` — owned by `W3-T01B`; wait for endpoint contract because frontend must request fixture data from backend.
- `Fixture DTOs` — owned by `W3-T01A`; can work from agreed fields because pages need clubs, dates, statuses, and results.
- `Frontend auth/session flow` — owned by `W1-FE01C`; can work from session fields because admin-only fixture links may depend on role display.

### Developer Notes

- Show upcoming fixtures, completed fixtures, status, venue, and result information if available.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W3-T03A] Match Result and Statistic DAO/DTO Foundation

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, dto, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create DTOs and DAO methods for match results and player match statistics.

**Simple task explanation:** Complete the focused work for Match Result and Statistic DAO/DTO Foundation.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Match result request/response DTOs
- Player statistic request/response DTOs
- `MatchResultDAO`
- `PlayerMatchStatisticDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FixtureDAO` — owned by `W3-T01A`; wait for fixture lookup/status methods because results and stats belong to fixtures.
- `PlayerDAO` — owned by `W2-T01C`; wait for player lookup because statistics reference real players.
- `schema.sql` — owned by `W1-BE01`; wait for result/stat tables because DAO SQL must match database design.

### Developer Notes

- Statistic records should be granular enough for scoring rules.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W3-T03B] Match Processing Service

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create service-layer logic for capturing or correcting match results and player statistics while coordinating fixture status changes.

**Simple task explanation:** Complete the focused work for Match Processing Service.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `MatchProcessingService`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `FixtureDAO` to update fixture status after results/statistics are captured
- `FixtureStatus` if captured/completed/recalculation-needed statuses are missing

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Match result/stat DAOs and DTOs` — owned by `W3-T03A`; wait for persistence methods and payloads because service saves raw match data.
- `Fixture service/DAO` — owned by `W3-T01A/W3-T01B`; wait for status conventions because capture changes fixture status.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; can work from admin role contract because only admins may capture or correct match data.

### Developer Notes

- Corrections should mark fixtures as needing recalculation if points already exist.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T03C] Match Result and Statistic Resources

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create backend Resources for admin capture/correction of match results and player statistics.

**Simple task explanation:** Complete the focused work for Match Result and Statistic Resources.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `MatchResultResource`
- `MatchStatisticResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `MatchProcessingService` — owned by `W3-T03B`; wait for service methods because Resources must delegate capture rules and persistence.
- `Match result/stat DTOs` — owned by `W3-T03A`; can work from agreed fields because Resources need request/response payloads.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; wait before admin testing because capture endpoints require admin protection.

### Developer Notes

- Admins capture stats/results only; fantasy points are calculated elsewhere.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Resources accept HTTP/JSON requests, call services, and return JSON responses.
- Resources must not contain SQL or large business logic.
- Backend filters/services remain final permission authority.

### Acceptance Criteria

- [ ] Endpoint accepts the required request format.
- [ ] Endpoint returns the required JSON response format.
- [ ] Protected actions use backend auth/role checks.
- [ ] Resource contains no SQL or large business logic.
- [ ] Errors and empty states are handled clearly.

## [W3-T04A] Scoring Rule DAO and DTO Foundation

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, dto, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create scoring rule DTOs and DAO methods for storing, updating, and loading active fantasy scoring rules.

**Simple task explanation:** Complete the focused work for Scoring Rule DAO and DTO Foundation.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Scoring rule request/response DTOs
- `ScoringRuleDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `ScoringRule model` — owned by `W1-BE03`; wait for model fields because DTO and DAO mapping need scoring fields.
- `schema.sql` — owned by `W1-BE01`; wait for scoring rule table because DAO SQL must match database.
- `DBConnectionManager/BaseDAO` — owned by `W1-BE02`; wait for shared connection setup because DAO must use standard connection path.

### Developer Notes

- Support active-rule loading for calculation.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W3-T04B] Scoring Rule Service and Resource

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create admin scoring-rule service validation and backend Resource endpoints.

**Simple task explanation:** Complete the focused work for Scoring Rule Service and Resource.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `ScoringRuleService`
- `ScoringResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `ScoringRuleDAO/DTOs` — owned by `W3-T04A`; wait for DAO methods and payloads because service and Resource need persistence and DTOs.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; wait before admin testing because scoring-rule changes are admin-only.
- `ApiResponse/ErrorResponse` — owned by `W1-BE05`; can work from agreed format because invalid scoring values need JSON errors.

### Developer Notes

- Reject invalid scoring values and expose active rules for calculation.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T05A] Fantasy Point Transaction DAO

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, database, mvp  
**Priority:** Urgent  
**Difficulty:** Medium

### Description

Create DAO methods for saving, replacing, reversing, and loading fantasy point transactions by fixture, player, and team where required.

**Simple task explanation:** Complete the focused work for Fantasy Point Transaction DAO.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `FantasyPointTransactionDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FantasyPointTransaction model` — owned by `W1-BE03`; wait for model fields because DAO maps point transaction rows.
- `schema.sql` — owned by `W1-BE01`; wait for point transaction table because DAO SQL must match point storage.
- `DBConnectionManager/BaseDAO` — owned by `W1-BE02`; wait for shared DB setup because DAO must use standard connection path.

### Developer Notes

- Support replace/reversal by fixture so recalculation does not double-count.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W3-T05B] Fantasy Point Calculation Service

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** Urgent  
**Difficulty:** XXX HARD

### Description

Create the service that converts player match statistics into fantasy point transactions using active scoring rules and safe recalculation.

**Simple task explanation:** Complete the focused work for Fantasy Point Calculation Service.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `FantasyPointCalculationService`
- Calculation result DTOs

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `ScoringRuleService` to expose active scoring rules if needed
- `PlayerMatchStatisticDAO` to provide statistic-loading methods if missing
- `ScoringRuleDAO` to provide active-rule loading methods if missing

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Player match statistics` — owned by `W3-T03A/W3-T03B`; wait for stat persistence and capture flow because calculation needs raw stat records.
- `ScoringRuleService/DAO` — owned by `W3-T04A/W3-T04B`; wait for active-rule loading because calculation uses configured point values.
- `FantasyPointTransactionDAO` — owned by `W3-T05A`; wait for transaction persistence because calculated points must be saved and safely replaced.
- `FixtureStatus` — owned by `W3-T01A`; can work from agreed statuses because calculation should run only for valid fixture states.

### Developer Notes

- Do not let admins manually edit fantasy points.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T06A] Team Score Service and DAO Updates

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, dao, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Create backend logic to update fantasy team totals and weekly points from fantasy point transactions after calculation or recalculation.

**Simple task explanation:** Complete the focused work for Team Score Service and DAO Updates.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `TeamScoreService`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `FantasyTeamDAO` to update team totals/weekly points
- `FantasyPointTransactionDAO` to provide point totals for teams and players

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FantasyPointCalculationService` — owned by `W3-T05B`; wait for calculation result/transaction contract because team totals depend on calculated point transactions.
- `FantasyTeamDAO` — owned by `W2-T03B`; wait for team total update fields because score updates write to fantasy teams.
- `FantasyPointTransactionDAO` — owned by `W3-T05A`; wait for totals methods or add them here because score service aggregates transactions.

### Developer Notes

- Handle recalculation by replacing totals with fresh totals, not adding duplicates.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T06B] Leaderboard Refresh Integration

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Integrate team score updates with leaderboard refresh/query behavior so league rankings reflect current totals after scoring.

**Simple task explanation:** Complete the focused work for Leaderboard Refresh Integration.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- None expected for this ticket.

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `LeaderboardService` to refresh or read updated rankings after score changes
- `LeaderboardDAO` to rebuild or query refreshed rows if needed

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `TeamScoreService` — owned by `W3-T06A`; wait for score-update behavior because leaderboards rank teams by updated totals.
- `LeaderboardService/DAO` — owned by `W2-T08A`; wait for leaderboard foundation because this ticket extends existing read/query behavior.
- `LeagueMembershipDAO` — owned by `W2-T06A`; can work from agreed methods because private league leaderboard reads still require membership checks.

### Developer Notes

- Prefer deriving ranking from current team totals unless stored leaderboard rows are required.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T07A] User Points and Results History Backend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, service, resource, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create backend DAO, service, Resource, and DTO support for users to view their own fantasy points and results history.

**Simple task explanation:** Complete the focused work for User Points and Results History Backend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `HistoryDAO`
- `HistoryService`
- `HistoryResource`
- History response DTOs

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FantasyPointTransactionDAO` — owned by `W3-T05A`; wait for point transaction data because history reads calculated player/team points.
- `TeamScoreService` — owned by `W3-T06A`; can work from agreed totals because history should align with team totals.
- `Fixture/MatchResult data` — owned by `W3-T01A/W3-T03A`; wait for fixture/result fields because history displays result context.
- `AuthFilter` — owned by `W1-BE07`; wait before protected testing because users may only view their own private history.

### Developer Notes

- Handle empty history clearly.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T07B] User Points and Results History Frontend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Create frontend page, servlet, and REST client for displaying a user fantasy points/results history.

**Simple task explanation:** Complete the focused work for User Points and Results History Frontend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `HistoryRestClient`
- `HistoryServlet`
- `history.jsp`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add History link

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `HistoryResource` — owned by `W3-T07A`; wait for endpoint contract because frontend must load history from backend.
- `Frontend auth/session flow` — owned by `W1-FE01C`; wait for logged-in support because history is a protected user page.
- `History DTOs` — owned by `W3-T07A`; can work from agreed fields because page needs points, fixture, dates, and result context.

### Developer Notes

- Do not compute official points in JavaScript.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W3-T08A] Match Simulation Service

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create simple match simulation logic that generates match result and player statistic data for a fixture using available players and controlled randomness.

**Simple task explanation:** Complete the focused work for Match Simulation Service.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `MatchSimulationService`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `FixtureService/DAO` — owned by `W3-T01A/W3-T01B`; wait for fixture lookup and statuses because simulation runs for a selected fixture.
- `PlayerDAO` — owned by `W2-T01C`; wait for player lookup by club/availability because simulation needs eligible players.
- `MatchProcessingService` — owned by `W3-T03B`; can work from agreed save/process method because simulated data should follow the manual processing path.

### Developer Notes

- Use simple, explainable randomness suitable for a student demo.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T08B] Simulation Resource and Processing Integration

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create the admin-protected Resource endpoint that triggers match simulation and routes generated data through normal match processing.

**Simple task explanation:** Complete the focused work for Simulation Resource and Processing Integration.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `SimulationResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `MatchProcessingService` so simulated results follow the same save/process path
- `MatchResultDAO` or `PlayerMatchStatisticDAO` only if simulated-save support is missing

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `MatchSimulationService` — owned by `W3-T08A`; wait for simulation method because Resource triggers generated data.
- `MatchProcessingService` — owned by `W3-T03B`; wait for processing path because simulated data must save like manual data.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; wait before admin testing because only admins may run simulation.

### Developer Notes

- Do not create a separate scoring path for simulated matches.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Resources accept HTTP/JSON requests, call services, and return JSON responses.
- Resources must not contain SQL or large business logic.
- Backend filters/services remain final permission authority.

### Acceptance Criteria

- [ ] Endpoint accepts the required request format.
- [ ] Endpoint returns the required JSON response format.
- [ ] Protected actions use backend auth/role checks.
- [ ] Resource contains no SQL or large business logic.
- [ ] Errors and empty states are handled clearly.

## [W3-T09A] Controlled Re-Simulation Service Rules

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Add service-layer rules for safely re-simulating an already simulated match only when project rules allow it.

**Simple task explanation:** Complete the focused work for Controlled Re-Simulation Service Rules.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- None expected for this ticket.

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `MatchSimulationService` to add re-simulation rules
- `MatchProcessingService` to replace/update old simulated result data safely
- `FantasyPointCalculationService` to support recalculation without double-counting if missing

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Simulation service` — owned by `W3-T08A`; wait for initial simulation behavior because re-simulation extends simulation.
- `FantasyPointCalculationService` — owned by `W3-T05B`; wait for safe recalculation behavior because old points must not be double-counted.
- `TeamScoreService/Leaderboard refresh` — owned by `W3-T06A/W3-T06B`; can work from agreed refresh calls because scores and rankings must update after recalculation.

### Developer Notes

- Do not allow unlimited re-simulation after users have seen final points unless explicitly approved.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W3-T09B] Re-Simulation API Integration

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, resource, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Add the protected Resource endpoint for controlled re-simulation and connect it to recalculation, score refresh, and leaderboard refresh behavior.

**Simple task explanation:** Complete the focused work for Re-Simulation API Integration.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- None expected for this ticket.

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `SimulationResource` to add controlled re-simulation endpoint
- `LeaderboardService` if explicit refresh is needed after recalculation

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Re-simulation service rules` — owned by `W3-T09A`; wait for service behavior because Resource must delegate fairness and recalculation decisions.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; wait before admin testing because endpoint is admin-only.
- `ApiResponse/ErrorResponse` — owned by `W1-BE05`; can work from agreed format because blocked re-simulation needs clear JSON errors.

### Developer Notes

- Return a summary of what changed and whether recalculation ran.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Resources accept HTTP/JSON requests, call services, and return JSON responses.
- Resources must not contain SQL or large business logic.
- Backend filters/services remain final permission authority.

### Acceptance Criteria

- [ ] Endpoint accepts the required request format.
- [ ] Endpoint returns the required JSON response format.
- [ ] Protected actions use backend auth/role checks.
- [ ] Resource contains no SQL or large business logic.
- [ ] Errors and empty states are handled clearly.

## [W3-T10] Match Processing Integration Tests

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** integration, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create and complete the Week 3 checklist for fixture creation, manual results, stats, scoring rules, point calculation, score refresh, leaderboards, history, simulation, and recalculation.

**Simple task explanation:** Complete the focused work for Match Processing Integration Tests.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `week-3-match-processing-checklist.md`
- Week 3 GitHub bug notes/issues for anything that fails

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `TESTING.md` to add Week 3 match-processing test results

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Week 3 implementation tickets` — owned by `W3-T01A through W3-T09B`; wait for features before final checklist completion because tests must verify real integrated behavior.
- `Week 2 foundation` — owned by `W2-T01A through W2-T10`; wait for teams, players, leagues, and leaderboards because scoring requires Week 2 data.
- `Frontend fixture/history pages` — owned by `W3-T02A/W3-T07B`; wait for frontend checks because full demo includes frontend display.

### Developer Notes

- Test manual and simulated match processing separately.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Testing should verify the full frontend-to-backend-to-database flow where applicable.
- Record bugs clearly instead of hiding them inside broad tickets.
- Do not add unrelated feature scope during verification.

### Acceptance Criteria

- [ ] Checklist or test evidence covers the named features.
- [ ] Permission and ownership cases are tested where relevant.
- [ ] Errors or bugs are recorded clearly.
- [ ] Correct layer responsibility is verified.
- [ ] The ticket is ready for the next dependent work.

---
# Week 4 — Social Features, Admin Tools, Final Testing, and Demo Polish

Week 4 proves the system is safe, complete, and demo-ready.

## [W4-T01A] League Chat DAO and DTO Foundation

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, dto, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create chat message DTOs and DAO methods for saving, loading, and hiding league chat messages.

**Simple task explanation:** Complete the focused work for League Chat DAO and DTO Foundation.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Chat request/response DTOs
- `ChatMessageDAO`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- None expected for this ticket.

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `ChatMessage model` — owned by `W1-BE03`; wait for model fields because DTOs and DAO mapping need message fields.
- `LeagueMembershipDAO` — owned by `W2-T06A`; can work from agreed league/member ids because messages belong to leagues and users.
- `schema.sql` — owned by `W1-BE01`; wait for chat table because DAO SQL must match database design.

### Developer Notes

- Use soft removal rather than physical deletion unless schema requires deletion.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- DAOs own SQL, row mapping, and persistence only.
- Services own validation and permission rules.
- Resources and frontend code must not open database connections.

### Acceptance Criteria

- [ ] Required DAO methods compile or are documented clearly.
- [ ] SQL is only in DAO classes.
- [ ] Rows map to the expected models/DTOs.
- [ ] Invalid or missing database data is handled clearly.
- [ ] The ticket is ready for service-layer work.

## [W4-T01B] League Chat Service and Resource

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, resource, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Create backend service and Resource endpoints for league members to read and send chat messages.

**Simple task explanation:** Complete the focused work for League Chat Service and Resource.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `ChatService`
- `ChatResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Chat DAO/DTOs` — owned by `W4-T01A`; wait for persistence and payloads because service and Resource need message storage and response shapes.
- `LeagueService/membership checks` — owned by `W2-T06B/W2-T07A`; wait for membership behavior because only league members may read/send chat.
- `AuthFilter` — owned by `W1-BE07`; wait before protected testing because chat requires logged-in user identity.

### Developer Notes

- Hide removed messages from normal display.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W4-T01C] League Chat Frontend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Create the frontend page, servlet, and REST client for displaying and sending league chat messages.

**Simple task explanation:** Complete the focused work for League Chat Frontend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `ChatRestClient`
- `ChatServlet`
- `league-chat.jsp`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add League Chat link

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `ChatResource` — owned by `W4-T01B`; wait for endpoint contract because frontend must read/send chat through backend.
- `Frontend auth/session flow` — owned by `W1-FE01C`; wait for logged-in support because chat is protected.
- `League id source` — owned by `W2-T06C/W2-T07A`; can work from agreed league id source because chat needs the current league.

### Developer Notes

- Display empty chat, send failures, and forbidden errors clearly.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W4-T02A] Chat Report Backend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, service, resource, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Create backend report DTOs, DAO, service, and Resource endpoints for users to report visible league chat messages.

**Simple task explanation:** Complete the focused work for Chat Report Backend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Report request/response DTOs
- `ReportDAO`
- `ReportService`
- `ReportResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Report model/status` — owned by `W1-BE03`; wait for model and enum fields because reports need status and audit fields.
- `ChatMessageDAO` — owned by `W4-T01A`; wait for message lookup because service must verify the reported message exists and is visible.
- `League membership checks` — owned by `W2-T06B`; wait for membership behavior because users should report only messages they may view.
- `AuthFilter` — owned by `W1-BE07`; wait before protected testing because reporting requires logged-in identity.

### Developer Notes

- Users can create reports but cannot resolve them.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W4-T02B] Chat Report Frontend Action

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Easy

### Description

Add a report action to league chat and connect it to the backend report API through frontend servlet/client flow.

**Simple task explanation:** Complete the focused work for Chat Report Frontend Action.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- None expected for this ticket.

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `league-chat.jsp` to add report action
- `ChatServlet` or report-specific route if needed
- `ChatRestClient` or small report client if separated

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `ReportResource` — owned by `W4-T02A`; wait for endpoint contract because frontend must submit reports to backend.
- `Chat frontend` — owned by `W4-T01C`; wait for message display structure because report buttons attach to visible messages.
- `Frontend auth/session flow` — owned by `W1-FE01C`; wait for logged-in support because reporting is protected.

### Developer Notes

- Do not expose moderation controls to normal users.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W4-T03A] Chat Moderation Backend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, resource, mvp  
**Priority:** High  
**Difficulty:** Hard

### Description

Create moderation service and Resource support for admins and league managers to review reports and hide inappropriate messages.

**Simple task explanation:** Complete the focused work for Chat Moderation Backend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `ModerationService`
- `ModerationResource`
- Moderation request/response DTOs

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `ReportDAO` to add review/status update methods
- `ChatMessageDAO` to add remove/hide methods
- `ReportResource` if report listing/status endpoints belong there

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Report backend` — owned by `W4-T02A`; wait for report persistence because moderation reviews saved reports.
- `Chat DAO` — owned by `W4-T01A`; wait for hide/remove methods or add them here because moderation hides inappropriate messages.
- `LeagueService/membership roles` — owned by `W2-T06B`; wait for league manager checks because league managers moderate only their leagues.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; wait before protected testing because admins and managers need different permissions.

### Developer Notes

- Admins moderate all leagues; league managers moderate only their leagues.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W4-T03B] Moderation Frontend or API Handoff

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, integration, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Provide the minimum frontend or API handoff needed for moderators to use moderation during the demo.

**Simple task explanation:** Complete the focused work for Moderation Frontend or API Handoff.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `moderation.jsp` and `ModerationRestClient` only if the team chooses a UI
- Moderation demo notes if API/manual testing is chosen

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared admin/manager navigation only if a UI is built
- `DEMO_SCRIPT.md` if moderation is demonstrated through API calls

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `ModerationResource` — owned by `W4-T03A`; wait for endpoint contract because handoff must match real endpoints.
- `Frontend auth/session flow` — owned by `W1-FE01C`; can work from role fields because UI navigation may depend on admin/manager role.
- `Demo docs` — owned by `W4-T09A`; coordinate with docs owner because manual moderation steps must be captured.

### Developer Notes

- Keep this minimal: simple page or precise API demo steps.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W4-T04A] Notification Backend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, service, resource, mvp  
**Priority:** Medium  
**Difficulty:** Hard

### Description

Create backend notification DTOs, DAO, service, and Resource endpoints for in-app notifications and mark-as-read behavior.

**Simple task explanation:** Complete the focused work for Notification Backend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Notification request/response DTOs
- `NotificationDAO`
- `NotificationService`
- `NotificationResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Notification model/type` — owned by `W1-BE03`; wait for model and enum fields because notifications need user, type, read status, and message fields.
- `AuthFilter` — owned by `W1-BE07`; wait before protected testing because users may only read their own notifications.
- `Feature events` — owned by `W3-T06A/W4-T03A/project lead`; can work from agreed event calls because some notifications are created by scoring or moderation.

### Developer Notes

- Keep notification generation simple for MVP.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W4-T04B] Notification Frontend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Create frontend page, servlet, and REST client for listing notifications and marking them as read.

**Simple task explanation:** Complete the focused work for Notification Frontend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `NotificationRestClient`
- `NotificationServlet`
- `notifications.jsp`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add Notifications link

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `NotificationResource` — owned by `W4-T04A`; wait for endpoint contract because frontend must load/update notifications through backend.
- `Frontend auth/session flow` — owned by `W1-FE01C`; wait for logged-in support because notifications are user-specific.
- `Notification DTOs` — owned by `W4-T04A`; can work from agreed fields because page needs message, type, date, and read status.

### Developer Notes

- Show empty notification state clearly.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W4-T05A] Private Message Backend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, dao, service, resource, enhancement  
**Priority:** Low  
**Difficulty:** Hard

### Description

Create backend DTOs, DAO, service, and Resource endpoints for private messages if this enhancement remains in scope.

**Simple task explanation:** Complete the focused work for Private Message Backend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Private message request/response DTOs
- `PrivateMessageDAO`
- `PrivateMessageService`
- `PrivateMessageResource`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `RestApplication` / backend JAX-RS configuration

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `PrivateMessage model` — owned by `W1-BE03`; wait for model fields because DAO and DTO mapping need message fields.
- `UserDAO` — owned by `W1-BE08`; wait for user lookup methods because service must validate sender and receiver users.
- `AuthFilter` — owned by `W1-BE07`; wait before protected testing because users may only read messages they sent or received.

### Developer Notes

- Keep this separate from league group chat.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W4-T05B] Private Message Frontend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, enhancement  
**Priority:** Low  
**Difficulty:** Medium

### Description

Create frontend page, servlet, and REST client for private messaging if the enhancement remains in scope.

**Simple task explanation:** Complete the focused work for Private Message Frontend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `PrivateMessageRestClient`
- `PrivateMessageServlet`
- `messages.jsp`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add Messages link

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `PrivateMessageResource` — owned by `W4-T05A`; wait for endpoint contract because frontend sends and loads messages through backend.
- `Frontend auth/session flow` — owned by `W1-FE01C`; wait for logged-in support because messages are user-specific.
- `User lookup/search contract` — owned by `W1-BE08 or W4-T06A`; can work from agreed recipient lookup because users need a way to select recipients.

### Developer Notes

- This can remain optional/enhancement if time is short.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W4-T06A] Admin Search and Reports Backend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** backend, service, resource, mvp  
**Priority:** Medium  
**Difficulty:** Hard

### Description

Create backend admin search and basic report APIs for users, leagues, players, fixtures, teams, reports, and useful aggregate data.

**Simple task explanation:** Complete the focused work for Admin Search and Reports Backend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `AdminResource`
- `AdminReportService`
- `AdminSearchService`
- Admin search/report response DTOs

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Existing DAOs used for admin reports/search as needed: `UserDAO`, `LeagueDAO`, `PlayerDAO`, `FixtureDAO`, `FantasyTeamDAO`, `ReportDAO`, `ChatMessageDAO`

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Feature DAOs` — owned by `Weeks 1 to 4 feature tickets`; wait for implemented DAO methods or add narrow query methods here because admin reports aggregate real system data.
- `AuthFilter/RoleFilter` — owned by `W1-BE07`; wait before protected testing because admin APIs must block normal users.
- `Report/moderation data` — owned by `W4-T02A/W4-T03A`; can work from agreed tables if moderation is included because admin reports may include report activity.

### Developer Notes

- Use real database queries/aggregations; do not fake report data in frontend.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Services own business rules, validation, ownership checks, and permission coordination.
- DAOs own SQL and row mapping.
- Resources handle request/response mapping only.

### Acceptance Criteria

- [ ] Main business rule works through service methods.
- [ ] Required validation or ownership rule works.
- [ ] No SQL exists in service classes.
- [ ] Errors are clear enough for Resources/frontend to display.
- [ ] The ticket is ready for Resource or integration work.

## [W4-T06B] Admin Dashboard Frontend

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Create frontend admin dashboard/search/report pages and REST client calls for backend admin APIs.

**Simple task explanation:** Complete the focused work for Admin Dashboard Frontend.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `AdminRestClient`
- `admin-dashboard.jsp`
- `admin-search.jsp`
- `admin-reports.jsp`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Shared navigation/layout to add Admin links for admin users only

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `AdminResource` — owned by `W4-T06A`; wait for endpoint contract because frontend pages need real admin API responses.
- `Frontend auth/session flow` — owned by `W1-FE01C`; wait for role/session fields because admin links should show only for admins in UI.
- `Admin DTOs` — owned by `W4-T06A`; can work from agreed fields because tables and report cards need known response fields.

### Developer Notes

- Frontend hiding of admin links is UX only; backend admin checks remain final.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W4-T07A] Final Permission and Validation Audit

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** integration, mvp  
**Priority:** Urgent  
**Difficulty:** Medium

### Description

Create and complete final permission and validation checklists across implemented Resources, services, filters, and protected frontend pages.

**Simple task explanation:** Complete the focused work for Final Permission and Validation Audit.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `permission-checklist.md`
- `validation-checklist.md`
- Final permission/validation GitHub bug notes/issues for anything that fails

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `TESTING.md` to record audit results

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Implemented Resources/Services` — owned by `All MVP feature tickets`; wait for implemented endpoints before final audit completion because audit must check real behavior.
- `Auth/role filters` — owned by `W1-BE07`; wait for final auth behavior because permission checks depend on filters and user identity.
- `Frontend protected pages` — owned by `frontend feature tickets`; wait for protected routes because frontend UX guards should be checked too.

### Developer Notes

- Prioritize backend permission failures.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Testing should verify the full frontend-to-backend-to-database flow where applicable.
- Record bugs clearly instead of hiding them inside broad tickets.
- Do not add unrelated feature scope during verification.

### Acceptance Criteria

- [ ] Checklist or test evidence covers the named features.
- [ ] Permission and ownership cases are tested where relevant.
- [ ] Errors or bugs are recorded clearly.
- [ ] Correct layer responsibility is verified.
- [ ] The ticket is ready for the next dependent work.

## [W4-T07B] Final Permission and Validation Bug Fixes

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** bug, integration, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Fix confirmed permission and validation bugs found by `W4-T07A`; do not add new feature scope.

**Simple task explanation:** Complete the focused work for Final Permission and Validation Bug Fixes.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- None expected for this ticket.

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Affected Resources/Services/filters only for confirmed bugs
- Affected frontend protected pages only for UX guard fixes

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Permission/validation audit` — owned by `W4-T07A`; wait for confirmed bug list because this ticket should fix actual findings.
- `Relevant feature owners` — owned by `owning feature tickets`; coordinate before editing another ticket files because avoid duplicate or conflicting fixes.

### Developer Notes

- Trace each fix to a checklist finding or bug note.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Fixes must preserve the separated frontend/backend WAR architecture.
- SQL stays in DAOs and business rules stay in services.
- Retest only the affected flow plus any direct dependents.

### Acceptance Criteria

- [ ] Confirmed bug is fixed or explicitly documented if deferred.
- [ ] Retest evidence is recorded.
- [ ] No unrelated feature work is added.
- [ ] Layer responsibilities remain correct.
- [ ] The fix is ready for final integration testing.

## [W4-T08A] Final Integration Testing Checklist

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** integration, mvp  
**Priority:** Urgent  
**Difficulty:** Hard

### Description

Create and execute the final full-MVP checklist from a clean database through registration, gameplay, scoring, social features, moderation, notifications, and demo docs where in scope.

**Simple task explanation:** Complete the focused work for Final Integration Testing Checklist.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `final-integration-checklist.md`
- Final integration GitHub bug notes/issues for anything that fails

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- `TESTING.md` to record final integration results

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Full MVP frontend and backend` — owned by `all MVP feature tickets`; wait for implemented flow before final completion because integration testing must cover the actual app.
- `Database scripts` — owned by `W1-BE01/W1-BE04`; wait for clean reset path because final test starts from reset schema and seed data.
- `Setup/demo docs` — owned by `W4-T09A/W4-T09B`; can coordinate while testing because test steps should align with documented setup.

### Developer Notes

- Run the demo flow from clean database reset.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Testing should verify the full frontend-to-backend-to-database flow where applicable.
- Record bugs clearly instead of hiding them inside broad tickets.
- Do not add unrelated feature scope during verification.

### Acceptance Criteria

- [ ] Checklist or test evidence covers the named features.
- [ ] Permission and ownership cases are tested where relevant.
- [ ] Errors or bugs are recorded clearly.
- [ ] Correct layer responsibility is verified.
- [ ] The ticket is ready for the next dependent work.

## [W4-T08B] Final Integration Bug Fixes

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** bug, integration, mvp  
**Priority:** Urgent  
**Difficulty:** XXX HARD

### Description

Fix major bugs found by the final integration checklist. Scope this ticket to confirmed blockers and high-priority defects only.

**Simple task explanation:** Complete the focused work for Final Integration Bug Fixes.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- None expected for this ticket.

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Only files/classes needed to fix confirmed integration bugs

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Final integration checklist` — owned by `W4-T08A`; wait for bug list because fixes should address confirmed failures.
- `Feature owners` — owned by `owning feature tickets`; coordinate before editing another feature code because avoid conflicts and duplicated fixes.
- `TESTING.md` — owned by `W4-T08A`; update after retesting because bug fixes need verification evidence.

### Developer Notes

- Prioritize demo blockers, data-corruption risks, permission bugs, and broken setup.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Fixes must preserve the separated frontend/backend WAR architecture.
- SQL stays in DAOs and business rules stay in services.
- Retest only the affected flow plus any direct dependents.

### Acceptance Criteria

- [ ] Confirmed bug is fixed or explicitly documented if deferred.
- [ ] Retest evidence is recorded.
- [ ] No unrelated feature work is added.
- [ ] Layer responsibilities remain correct.
- [ ] The fix is ready for final integration testing.

## [W4-T09A] Setup and Demo Documentation

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** documentation, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Write setup, troubleshooting, demo-script, and test-account documentation so another person can run the project from scratch.

**Simple task explanation:** Complete the focused work for Setup and Demo Documentation.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `README.md`
- `SETUP.md`
- `DEMO_SCRIPT.md`
- `TEST_ACCOUNTS.md`
- `TROUBLESHOOTING.md`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Existing setup docs only if they already exist and must be cleaned up

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Backend WAR run configuration` — owned by `project team`; wait for final ports/context paths because setup steps must match deployable backend.
- `Frontend WAR run configuration` — owned by `project team`; wait for final ports/context paths because setup steps must match deployable frontend.
- `SQL scripts and seed accounts` — owned by `W1-BE01/W1-BE04`; wait for final reset/seed path because docs need reproducible database setup.
- `Final integration results` — owned by `W4-T08A`; can update as testing confirms because demo script should match tested flow.

### Developer Notes

- Write steps for someone starting from zero.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Documentation must describe the separated frontend WAR and backend WAR accurately.
- Docs must not tell developers to bypass the backend with direct database access.
- API docs should match real Resource endpoints and DTOs.

### Acceptance Criteria

- [ ] Another student can follow the document without guessing.
- [ ] Commands, URLs, accounts, or payloads match the implemented app.
- [ ] Separated frontend/backend WAR architecture is clear.
- [ ] Known issues or assumptions are listed.
- [ ] The document is ready for handoff.

## [W4-T09B] API Contract Documentation

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** documentation, integration, mvp  
**Priority:** High  
**Difficulty:** Medium

### Description

Write the backend API contract used by frontend REST clients and testers, including auth, endpoint URLs, roles, payload examples, status codes, and errors.

**Simple task explanation:** Complete the focused work for API Contract Documentation.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- `API_CONTRACT.md`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Existing endpoint docs only if they should be consolidated

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Implemented backend Resources` — owned by `all backend Resource tickets`; wait for endpoint paths and payloads because contract must match real APIs.
- `Auth mechanism` — owned by `W1-BE06/W1-BE07`; wait for final token/session/header/cookie contract because protected endpoints depend on consistent auth.
- `Error response format` — owned by `W1-BE05`; wait for final JSON error shape because frontend error display depends on consistent payloads.

### Developer Notes

- Keep examples safe: no passwords, hashes, or private tokens in committed docs.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Documentation must describe the separated frontend WAR and backend WAR accurately.
- Docs must not tell developers to bypass the backend with direct database access.
- API docs should match real Resource endpoints and DTOs.

### Acceptance Criteria

- [ ] Another student can follow the document without guessing.
- [ ] Commands, URLs, accounts, or payloads match the implemented app.
- [ ] Separated frontend/backend WAR architecture is clear.
- [ ] Known issues or assumptions are listed.
- [ ] The document is ready for handoff.

## [W4-T10A] Frontend Layout and Navigation Cleanup

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Create or standardize shared frontend layout fragments and navigation across implemented JSP pages for the final demo.

**Simple task explanation:** Complete the focused work for Frontend Layout and Navigation Cleanup.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- Shared JSP layout fragment such as `header.jspf`
- Shared JSP navigation fragment such as `nav.jspf`
- Shared JSP footer fragment such as `footer.jspf`

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- All existing JSP pages for consistent layout/navigation
- Existing CSS/Tailwind files for styling cleanup

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Implemented frontend pages` — owned by `frontend feature tickets`; wait for pages before final cleanup because cleanup should standardize real pages.
- `Frontend auth/session flow` — owned by `W1-FE01C`; can work from role/session fields because navigation needs logged-in/admin-aware links.
- `Backend API responses` — owned by `backend feature tickets`; can work from final route names because navigation should point to real working flows.

### Developer Notes

- Do not rewrite feature logic while polishing layout.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

## [W4-T10B] Frontend Error Handling and Protected Page Polish

**State:** UnassignedDefault  
**Suggested Owner:** Unassigned  
**Labels:** frontend, mvp  
**Priority:** Medium  
**Difficulty:** Medium

### Description

Standardize frontend redirects, protected-page guards, loading states, and backend error display across existing frontend servlets, JSP pages, and REST clients.

**Simple task explanation:** Complete the focused work for Frontend Error Handling and Protected Page Polish.

**Why this matters:** This keeps the work small enough for one student to own while preserving the existing project architecture and scope.

### MUST CREATE

These are brand-new deliverables owned by this ticket. Create these items in the correct project layer. Do not create duplicate versions of items owned by another ticket.

- None expected for this ticket.

### MUST EDIT

These are existing deliverables this ticket must change. Edit these in place. Do not recreate them under a new name.

- Existing frontend servlets for redirects/error handling
- Existing REST clients for API error handling
- Existing JSP pages for success/error/empty-state display

### MUST USE / WAIT FOR FROM OTHER TICKETS

These are dependencies or reference items. Do not recreate them in this ticket unless they are also listed under **MUST CREATE** or **MUST EDIT** for this same ticket.

- `Frontend pages and REST clients` — owned by `frontend feature tickets`; wait for implemented flows because polish should standardize real behavior.
- `ApiResponse/ErrorResponse` — owned by `W1-BE05`; wait for final response format because frontend error handling must parse backend errors consistently.
- `Auth/session frontend flow` — owned by `W1-FE01C`; wait for session behavior because protected-page redirects depend on login state.

### Developer Notes

- Do not duplicate backend validation rules in JavaScript.
- Keep the ticket focused.
- Do not pull unrelated work into this ticket.

### Architectural Responsibility Notes

- Frontend follows JSP/Page -> Frontend Servlet -> REST Client -> Backend Resource.
- Frontend must not use DAOs, SQL, password hashing, or final permission logic.
- Backend remains final authority for validation and permissions.

### Acceptance Criteria

- [ ] Page/servlet/client flow calls backend APIs through REST clients.
- [ ] No database access exists in frontend code.
- [ ] Backend errors display clearly.
- [ ] Logged-out or forbidden states are handled where relevant.
- [ ] The ticket is ready for testing or dependent UI work.

---
# Final MVP Flow

- Register or log in
- View or update own profile if profile management is in the agreed scope
- Search/view players
- Create fantasy team
- Join public/private league
- View league leaderboard
- Admin creates fixture
- Admin enters stats or runs simulation
- System calculates fantasy points
- Team scores update
- Leaderboard refreshes
- User views points/history
- User sends league chat message
- User reports inappropriate message
- Admin or league manager moderates report
- User receives/views notification if included

# Consistent Implementation Standards

## Backend Ticket Pattern

Use this pattern for backend feature tasks unless the ticket clearly says it is database-only, DTO-only, or testing-only:

1. Create or update request/response DTOs.
2. Create or update DAO SQL and row mapping where persistence is needed.
3. Add service-layer validation, ownership, and permission checks.
4. Create or update the JAX-RS Resource endpoint.
5. Keep multi-table writes transactional so partial saves do not corrupt teams, transfers, results, or leaderboards.
6. Return `ApiResponse` / `ErrorResponse` consistently.
7. Add or update smoke/manual tests in the matching checklist.

## Shared API Standards

- Use one documented auth contract across login, protected resources, frontend REST clients, and setup docs.
- Use one documented error format and HTTP status-code approach across all backend resources, not only auth endpoints.
- Use one agreed date/time format and timezone rule in DTOs, SQL seed data, and demo docs.
- If a backend operation touches multiple tables in one business action, the service/DAO flow must either commit all related changes or roll them back together.

## Frontend Ticket Pattern

Use this pattern for frontend feature tasks:

1. Create or update JSP page and form/table layout.
2. Create or update frontend servlet only where browser form handling is needed.
3. Create or update REST client class for backend API calls.
4. Display backend success/error responses clearly.
5. Add page-level guards for user experience, while remembering backend protection is final.

## PR / Review Checklist

Before a ticket is marked done, check:

- The correct WAR was changed.
- The correct layer owns the logic.
- No frontend DAO/database access was added.
- No SQL was added to Resources, Servlets, JSP, or JavaScript.
- Protected features check login/role/ownership in the backend.
- DTOs do not expose passwords, BCrypt hashes, salts, or private data.
- The relevant testing checklist was updated.

# Final Safety Rules

- Do not store plain text passwords.
- Use secure one-way password hashing through `PasswordUtil` backed by BCrypt.
- Do not expose passwords or BCrypt hashes in API responses.
- Do not allow users to access another user's private data.
- Do not expose private league data to non-members.
- Do not let admins manually edit fantasy points.
- Do not fake demo data through frontend-only hardcoding.
- Do not allow frontend servlets to use DAOs.
- Do not put SQL in REST Resources.
- Do not put business rules in JSP pages.
- Do not let optional polish block the MVP.
