package com.vzap.trytons.service.fixture;

import com.vzap.trytons.dao.fixture.FantasyRoundDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.fixture.FixtureDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dao.results.MatchResultDAO;
import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.fixture.FixtureResponseDTO;
import com.vzap.trytons.enums.FantasyRoundStatus;
import com.vzap.trytons.enums.FixtureStatus;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.util.LeagueVisibility;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.fixture.FantasyRound;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.fixture.Fixture;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.league.LeagueMembership;
import com.vzap.trytons.model.results.MatchResult;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
public class FixtureServiceImpl implements FixtureService {
    @Inject
    private FixtureDAO fixtureDAO;
    @Inject
    private LeagueDAO leagueDAO;
    @Inject
    private FantasyRoundDAO fantasyRoundDAO;
    @Inject
    private FantasyTeamDAO fantasyTeamDAO;
    @Inject
    private LeagueMembershipDAO leagueMembershipDAO;
    @Inject
    private MatchResultDAO matchResultDAO;
    @Inject
    private UserDAO userDAO;

    @Override
    public List<FixtureResponseDTO> listFixtures(UUID actorUserId, FixtureStatus status, UUID leagueId) {
        if (actorUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        List<Fixture> fixtures;

        if (leagueId != null) {
            assertCanViewLeagueFixtures(leagueId, actorUserId);
            fixtures = fixtureDAO.findByLeagueId(leagueId);
            if (status != null) {
                fixtures = filterByStatus(fixtures, status);
            }
        } else if (isAdministrator(actorUserId)) {
            // Administrators oversee every league, so the unscoped view stays
            // exactly what it was before this endpoint learned to scope by
            // membership.
            fixtures = status != null ? fixtureDAO.findByStatus(status) : fixtureDAO.getAllFixtures();
        } else {
            // A plain SELECT * FROM fixture with no filter let every signed-in
            // user see every league's fixtures, private ones included. Scoped
            // to the leagues the caller actually belongs to instead.
            fixtures = fixturesForMemberLeagues(actorUserId, status);
        }

        Map<UUID, Integer> roundNumberCache = new HashMap<>();
        List<FixtureResponseDTO> responseList = new ArrayList<>();
        for(Fixture fixture : fixtures){
            FixtureResponseDTO response = mapToResponse(fixture, roundNumberCache);
            responseList.add(response);
        }

        return responseList;
    }

    private List<Fixture> filterByStatus(List<Fixture> fixtures, FixtureStatus status) {
        List<Fixture> filtered = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            if (fixture.getStatus() == status) {
                filtered.add(fixture);
            }
        }
        return filtered;
    }

    private List<Fixture> fixturesForMemberLeagues(UUID actorUserId, FixtureStatus status) {
        List<Fixture> fixtures = new ArrayList<>();
        for (LeagueMembership membership : leagueMembershipDAO.findActiveByUser(actorUserId)) {
            for (Fixture fixture : fixtureDAO.findByLeagueId(membership.getLeagueId())) {
                if (status == null || fixture.getStatus() == status) {
                    fixtures.add(fixture);
                }
            }
        }
        return fixtures;
    }

    private void assertCanViewLeagueFixtures(UUID leagueId, UUID actorUserId) {
        League league = leagueDAO.findLeagueById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found."));

        // Must mirror LeagueServiceImpl.getLeague's visibility check exactly: a caller
        // who can open a league's detail page (public leagues are visible to anyone;
        // private leagues need active membership or admin) must also be able to see
        // its fixtures, otherwise the "View fixtures" link on a public league renders
        // an empty/forbidden page for a non-member. Expressed through the shared
        // LeagueVisibility.canView predicate so the sites cannot drift apart again.
        boolean isActiveMember = leagueMembershipDAO.existsActiveByLeagueAndUser(leagueId, actorUserId);
        if (!LeagueVisibility.canView(league.getLeagueType(), isAdministrator(actorUserId), isActiveMember)) {
            throw new AuthorisationException("You must be an active member of this league to view its fixtures.");
        }
    }

    private boolean isAdministrator(UUID userId) {
        Optional<User> user = userDAO.getUserById(userId);
        return user.isPresent() && user.get().getRole() == UserRole.ADMINISTRATOR;
    }

    @Override
    public FixtureResponseDTO getFixture(UUID fixtureId) {
        if(fixtureId == null){
            throw new ValidationException("Fixture ID is required");
        }

        Optional<Fixture> fixture = fixtureDAO.findById(fixtureId);

        if(fixture.isEmpty()){
            throw new ResourceNotFoundException("Fixture not found");
        }

        Fixture fix = fixture.get();

        return mapToResponse(fix);
    }

    /*
        createFixture was removed along with POST /fixtures. Fixtures are
        generated by TournamentService when a league starts; creating them
        by hand would violate the one-fixture-per-team-per-round rule the
        generator relies on.
    */

    @Override
    public FixtureResponseDTO updateFixtureStatus(UUID actorUserId, UUID fixtureId, FixtureStatus status) {
        requireAdmin(actorUserId);

        if(fixtureId == null){
            throw new ValidationException("Fixture ID is required");
        }

        if(status == null){
            throw new ValidationException("Fixture status is required");
        }

        Optional<Fixture> fixtureOptional = fixtureDAO.findById(fixtureId);

        if(fixtureOptional.isEmpty()){
            throw new ResourceNotFoundException("Fixture not found");
        }

        Fixture fixture = fixtureOptional.get();

        if (!isValidStatusTransition(fixture.getStatus(), status)) {
            throw new BusinessRuleException("Cannot change fixture status.");
        }

        fixture.setStatus(status);

        /*
            Deliberately NOT stamping simulationDate here.

            simulationDate is evidence that a simulation actually ran; only
            MatchSimulationServiceImpl.simulateFixture may write it. This method
            used to stamp it whenever an administrator selected COMPLETED, which
            defused chk_fixture_simulation_date (the CHECK that a COMPLETED or
            PROCESSED fixture must carry a simulationDate) and let a hollow
            fixture — no matchResult, no playerStatistics, no match_team_score —
            pass for a played one. The triggers could not catch it either: they
            fire on INSERT/UPDATE of matchResult and match_team_score, and this
            path writes neither.

            Do not re-add it.
        */

        boolean updated = fixtureDAO.updateFixture(fixture);

        if (!updated) {
            throw new DataAccessException("Failed to update fixture status.", null);
        }

        return mapToResponse(fixture);

    }

    /*
        The administrative transition table.

        This endpoint is a bare persistence setter — it writes fixture.status and
        nothing else. So it may only own transitions that are purely
        administrative, i.e. ones that assert no outcome and leave no other table
        needing rows.

            UPCOMING  -> LOCKED, CANCELLED
            LOCKED    -> CANCELLED
            SIMULATING-> CANCELLED     (recovery only; nothing sets SIMULATING)
            COMPLETED -> (none)
            PROCESSED -> (none)
            CANCELLED -> (none)

        The transitions that are NOT here, and why:

          LOCKED -> SIMULATING     Removed. MatchSimulationServiceImpl.simulateFixture
                                   only accepts a LOCKED fixture, so parking one in
                                   SIMULATING made it permanently unsimulatable — a
                                   dead end whose only exit was CANCELLED. SIMULATING
                                   is kept in the enum for existing rows, but nothing
                                   moves a fixture into it.

          SIMULATING -> COMPLETED  Removed. COMPLETED means "this match was played".
          LOCKED     -> COMPLETED  It is set by MatchSimulationServiceImpl.simulateFixture,
                                   which writes the matchResult and playerStatistics that
                                   make it true. Use POST /simulations/fixtures/{id}.

          COMPLETED -> PROCESSED   Removed. PROCESSED means "fantasyPoints,
                                   match_team_score and the leaderboard have been
                                   brought up to date". It is set by
                                   MatchProcessingServiceImpl.processCompletedFixture,
                                   which does that work. Use
                                   POST /match-processing/fixtures/{id}.

        Letting an administrator select COMPLETED or PROCESSED here walked a
        fixture to "played and scored" in a few clicks with zero rows behind it.
    */
    private boolean isValidStatusTransition(
            FixtureStatus currentStatus,
            FixtureStatus newStatus) {

        if (currentStatus == null || newStatus == null) {
            return false;
        }

        if (currentStatus == FixtureStatus.UPCOMING) {
            return ((newStatus == FixtureStatus.LOCKED) || (newStatus == FixtureStatus.CANCELLED));
        }

        if (currentStatus == FixtureStatus.LOCKED) {
            return newStatus == FixtureStatus.CANCELLED;
        }

        if (currentStatus == FixtureStatus.SIMULATING) {
            return newStatus == FixtureStatus.CANCELLED;
        }

        return false;
    }

    private void  requireAdmin(UUID actorUserId) {
        if(actorUserId == null){
            throw new ValidationException("An authenticated administrator is required.");
        }

        Optional<User> userOptional = userDAO.getUserById(actorUserId);

        if(userOptional.isEmpty()){
            throw new AuthorisationException("An authenticated administrator is required.");
        }

        User user = userOptional.get();

        if(user.getRole() != UserRole.ADMINISTRATOR){
            throw new AuthorisationException("Only admins can perform this action.");
        }
    }

    private void assertActiveLeagueMember(UUID leagueId,UUID teamId, String teamName){
        List<LeagueMembership> memberships = leagueMembershipDAO.findActiveByLeague(leagueId);
        boolean found = false;

        for(LeagueMembership membership : memberships){
            if(membership.getTeamId().equals(teamId)){
                found = true;
                break;
            }
        }
        if(!found){
            throw new ValidationException(teamName + " is not an active member of this league.");
        }
    }

    private void assertNoDuplicatePairing(UUID roundId, UUID teamAId, UUID teamBId){
        List<Fixture> fixtures = fixtureDAO.findByRoundId(roundId);

        for(Fixture existingFixture : fixtures){
            if(existingFixture.getStatus() == FixtureStatus.CANCELLED){
                continue;
            }
            UUID existingTeamAId = existingFixture.getTeamAId();
            UUID existingTeamBId = existingFixture.getTeamBId();
            if(existingTeamAId.equals(teamBId) || existingTeamBId.equals(teamAId) || existingTeamBId.equals(teamBId) ||existingTeamAId.equals(teamAId)){
                throw new ConflictException("One of the teams already has a fixture in this round.");
            }
        }
    }

    private FixtureResponseDTO mapToResponse(Fixture fixture){
        return mapToResponse(fixture, new HashMap<>());
    }

    // roundNumberCache dedupes the fantasyRound lookup across a list of
    // fixtures that mostly share the same round; the matchResult lookup below
    // stays one-per-fixture, mirroring TournamentServiceImpl.getTournamentFixtures.
    private FixtureResponseDTO mapToResponse(Fixture fixture, Map<UUID, Integer> roundNumberCache){
        String teamAName = null;
        String teamBName = null;

        Optional<FantasyTeam> optionalTeamA = fantasyTeamDAO.getTeamById(fixture.getTeamAId());

        if(optionalTeamA.isPresent()){
            teamAName = optionalTeamA.get().getTeamName();
        }

        Optional<FantasyTeam> optionalTeamB = fantasyTeamDAO.getTeamById(fixture.getTeamBId());

        if(optionalTeamB.isPresent()){
            teamBName = optionalTeamB.get().getTeamName();
        }

        Integer roundNumber;
        if (roundNumberCache.containsKey(fixture.getRoundId())) {
            roundNumber = roundNumberCache.get(fixture.getRoundId());
        } else {
            roundNumber = fantasyRoundDAO.getRoundById(fixture.getRoundId())
                    .map(FantasyRound::getRoundNumber)
                    .orElse(null);
            roundNumberCache.put(fixture.getRoundId(), roundNumber);
        }

        Optional<MatchResult> result = matchResultDAO.findCurrentByFixtureId(fixture.getFixtureId());

        return mapToResponse(fixture, teamAName, teamBName, roundNumber,
                result.map(MatchResult::getTeamAScore).orElse(null),
                result.map(MatchResult::getTeamBScore).orElse(null));
    }

    private FixtureResponseDTO mapToResponse(Fixture fixture, String teamAName, String teamBName,
                                              Integer roundNumber, Integer teamAScore, Integer teamBScore){
        FixtureResponseDTO response = new FixtureResponseDTO();
        response.setFixtureId(fixture.getFixtureId());
        response.setLeagueId(fixture.getLeagueId());
        response.setRoundId(fixture.getRoundId());
        response.setRoundNumber(roundNumber);
        response.setStage(fixture.getStage());
        // Jackson writes the enum by name(), so the label has to travel as its
        // own field or the frontend is left reinventing it.
        response.setStageLabel(fixture.getStage() == null ? null : fixture.getStage().getLabel());
        response.setMatchdayNumber(fixture.getMatchdayNumber());
        response.setTeamAId(fixture.getTeamAId());
        response.setTeamAName(teamAName);
        response.setTeamBId(fixture.getTeamBId());
        response.setTeamBName(teamBName);
        response.setTeamAScore(teamAScore);
        response.setTeamBScore(teamBScore);
        response.setFixtureDate(fixture.getFixtureDate());
        response.setFixtureTime(fixture.getFixtureTime());
        response.setFixtureStatus(fixture.getStatus());
        response.setSimulationDate(fixture.getSimulationDate());
        response.setCreatedAt(fixture.getCreatedAt());

        return response;
    }
}