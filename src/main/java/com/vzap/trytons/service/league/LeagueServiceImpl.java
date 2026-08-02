package com.vzap.trytons.service.league;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.leaderboard.LeaderboardDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dto.league.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.league.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.league.LeagueMemberResponseDTO;
import com.vzap.trytons.dto.league.LeagueRequestDTO;
import com.vzap.trytons.dto.league.LeagueResponseDTO;
import com.vzap.trytons.dto.publicpreview.PublicLeaguePreviewDTO;
import com.vzap.trytons.enums.LeaderboardScope;
import com.vzap.trytons.enums.LeagueStatus;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.model.auth.User;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.leaderboard.Leaderboard;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.league.LeagueMembership;
import com.vzap.trytons.service.notification.NotificationService;
import com.vzap.trytons.service.shared.SeasonResolver;
import com.vzap.trytons.util.LeagueVisibility;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class LeagueServiceImpl implements LeagueService {
    private static final Logger LOG = Logger.getLogger(LeagueServiceImpl.class.getName());

    @Inject
    private LeagueDAO leagueDAO;

    @Inject
    private LeagueMembershipDAO membershipDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private LeaderboardDAO leaderboardDAO;

    @Inject
    private SeasonResolver seasonResolver;

    @Inject
    private NotificationService notificationService;

    @Override
    public LeagueResponseDTO createLeague(LeagueRequestDTO request, UUID currentUserId) {
        validateCreateLeagueRequest(request, currentUserId);

        String leagueName = request.getLeagueName().trim();

        if (leagueDAO.findLeagueByName(leagueName).isPresent()) {
            throw new ConflictException("A league with this name already exists.");
        }

        // Administrators run the competition rather than compete in it: they have no
        // fantasy team, and leagueMembership requires one, so an admin-created league
        // gets no founding member and no manager. Only public leagues make sense that
        // way — a private league is a group of friends around its own manager, and
        // nobody would be able to administer the code.
        boolean actorIsAdmin = isAdmin(currentUserId);
        if (actorIsAdmin && request.getLeagueType() != LeagueType.PUBLIC) {
            throw new BusinessRuleException(
                    "Administrators can only create public leagues. A private league needs a manager who plays in it.");
        }

        // The mirror of the rule above. A public league is part of the competition
        // proper -- it feeds the master leaderboard, tournament seeding, pricing and
        // market demand -- so it is run by an administrator, not by whoever happened
        // to click "create". A registered user's own league is a friendly: private,
        // joined with its league code, and scored only within itself.
        if (!actorIsAdmin && request.getLeagueType() != LeagueType.PRIVATE) {
            throw new BusinessRuleException(
                    "Registered users can only create private leagues. Public leagues are run by administrators.");
        }

        FantasyTeam team = actorIsAdmin
                ? null
                : fantasyTeamDAO.getTeamByOwner(currentUserId).orElseThrow(() -> new BusinessRuleException("You must create a fantasy team before creating a league."));

        League league = new League();
        league.setLeagueId(UUID.randomUUID());
        league.setLeagueName(leagueName);
        league.setDescription(request.getDescription());
        league.setLeagueType(request.getLeagueType());
        league.setMaxMembers(request.getMaxMembers());
        league.setIsActive(true);
        // The DAO defaults a null status to FORMING on insert; setting it here as well
        // means the DTO returned from this call reports the same status a later read would.
        league.setStatus(LeagueStatus.FORMING);

        if (request.getLeagueType() == LeagueType.PRIVATE) {
            league.setLeagueCode(generateLeagueCode());
        }

        League savedLeague = leagueDAO.createLeague(league);

        if (savedLeague == null || savedLeague.getLeagueId() == null) {
            throw new BusinessRuleException("The league could not be created.");
        }

        // An admin is neither a member nor the manager, so the league opens empty and
        // manager_user_id stays null — the column is nullable for exactly this case.
        if (!actorIsAdmin) {
            LeagueMembership managerMembership = membershipDAO.createMembership(
                    savedLeague.getLeagueId(), currentUserId, team.getTeamId());

            if (managerMembership == null || managerMembership.getMembershipId() == null) {
                throw new BusinessRuleException("The league manager membership could not be created.");
            }

            boolean managerAssigned = leagueDAO.assignManager(savedLeague.getLeagueId(), currentUserId);

            if (!managerAssigned) {
                throw new BusinessRuleException("The league manager could not be assigned.");
            }
        }

        createLeagueLeaderboard(savedLeague.getLeagueId());

        return toResponse(requireLeague(savedLeague.getLeagueId()));
    }

    /**
     * Best-effort: a board's absence must never fail a league creation that
     * otherwise succeeded (createLeague isn't one transaction, and the
     * membership/manager-assignment steps above already accept that same
     * risk). If the season calendar isn't configured yet, or the insert hits
     * something unexpected, this league simply stays without a board until
     * {@code LeaderboardServiceImpl.refreshLeagueLeaderboard}'s ensure-exists
     * step creates one lazily on first refresh.
     */
    private void createLeagueLeaderboard(UUID leagueId) {
        try {
            String season = seasonResolver.resolveCurrentSeason();
            Leaderboard board = Leaderboard.builder()
                    .leaderboardId(UUID.randomUUID())
                    .leagueId(leagueId)
                    .season(season)
                    .scope(LeaderboardScope.LEAGUE)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            leaderboardDAO.saveLeaderboard(board);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not create a leaderboard for league " + leagueId, e);
        }
    }

    @Override
    public LeagueResponseDTO getLeague(UUID leagueId, UUID currentUserId) {
        if (leagueId == null) {
            throw new ValidationException("League ID is required.");
        }

        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        League league = requireLeague(leagueId);

        // Administrators oversee every league without competing in any, which is
        // already how getAllLeagues lists them and how startLeague authorises them.
        // Withholding the detail here left an admin able to see a private league in
        // a list, and able to start it over the API, but unable to open its page.
        // The rule itself lives in LeagueVisibility.canView so this site, listMembers,
        // FixtureServiceImpl.assertCanViewLeagueFixtures and the leaderboard cannot
        // drift apart again -- they have done so twice.
        boolean isActiveMember = isLeagueMember(leagueId, currentUserId);
        if (!LeagueVisibility.canView(league.getLeagueType(), isAdmin(currentUserId), isActiveMember)) {
            throw new AuthorisationException("You are not permitted to view this private league.");
        }

        return toResponse(league);
    }

    @Override
    public boolean isLeagueMember(UUID leagueId, UUID userId) {
        if (leagueId == null || userId == null) {
            return false;
        }

        return membershipDAO.existsActiveByLeagueAndUser(leagueId, userId);
    }

    @Override
    public List<LeagueResponseDTO> getAllLeagues(UUID currentUserId) {
        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        boolean actorIsAdmin = isAdmin(currentUserId);
        List<League> leagues = leagueDAO.findAllLeagues();
        List<LeagueResponseDTO> responses = new ArrayList<>();

        for (League league : leagues) {
            if (actorIsAdmin || league.getLeagueType() == LeagueType.PUBLIC || isLeagueMember(league.getLeagueId(), currentUserId)) {
                responses.add(toResponse(league));
            }
        }

        return responses;
    }

    @Override
    public List<PublicLeaguePreviewDTO> getPublicLeaguePreviews(int limit) {
        List<PublicLeaguePreviewDTO> previews = new ArrayList<>();
        for (League league : leagueDAO.findAllLeagues()) {
            if (league.getLeagueType() != LeagueType.PUBLIC) {
                continue;
            }
            if (Boolean.FALSE.equals(league.getIsActive())) {
                continue;
            }
            previews.add(PublicLeaguePreviewDTO.builder()
                    .leagueName(league.getLeagueName())
                    .description(league.getDescription())
                    .maxMembers(league.getMaxMembers())
                    .memberCount(membershipDAO.countActiveMembers(league.getLeagueId()))
                    .build());
            if (limit > 0 && previews.size() >= limit) {
                break;
            }
        }
        return previews;
    }

    @Override
    public JoinLeagueResponseDTO joinLeague(JoinLeagueRequestDTO request, UUID currentUserId) {
        if (request == null) {
            throw new ValidationException("Join-league request cannot be null.");
        }
        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        if (request.getLeagueId() == null
                && request.getLeagueCode() != null && !request.getLeagueCode().isBlank()) {
            leagueDAO.findLeagueByLeagueCode(request.getLeagueCode().trim())
                    .ifPresent(found -> request.setLeagueId(found.getLeagueId()));
        }
      
        if (request.getTeamId() == null) {
            fantasyTeamDAO.getTeamByOwner(currentUserId)
                    .ifPresent(team -> request.setTeamId(team.getTeamId()));
        }

        validateJoinLeagueRequest(request, currentUserId);

        League league = requireLeague(request.getLeagueId());

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId()).orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found."));

        if (team.getOwnerUserId() == null || !currentUserId.equals(team.getOwnerUserId())) {
            throw new AuthorisationException("The selected fantasy team does not belong to the current user.");
        }

        if (membershipDAO.existsActiveByLeagueAndUser(league.getLeagueId(), currentUserId)) {
            throw new ConflictException("You are already a member of this league.");
        }

        if (league.getLeagueType() == LeagueType.PRIVATE) {
            validatePrivateLeagueCode(league, request.getLeagueCode());
        }

        if (membershipDAO.countActiveMembers(league.getLeagueId()) >= league.getMaxMembers()) {
            throw new ConflictException("This league is already full.");
        }

        LeagueMembership membership = membershipDAO.createMembership(
                league.getLeagueId(), currentUserId, request.getTeamId());

        if (membership == null || membership.getMembershipId() == null) {
            throw new BusinessRuleException("The league membership could not be created.");
        }

        notifyLeagueJoin(league, currentUserId, team);

        return JoinLeagueResponseDTO.builder()
                .leagueId(league.getLeagueId())
                .leagueName(league.getLeagueName())
                .message("Joined the league successfully.")
                .membershipId(membership.getMembershipId())
                .build();
    }

    @Override
    public List<LeagueMemberResponseDTO> listMembers(String actorUserId, String leagueId) {
        UUID actorId = parseUserId(actorUserId);
        UUID parsedLeagueId = parseLeagueId(leagueId);

        League league = requireLeague(parsedLeagueId);

        // Must mirror LeagueServiceImpl.getLeague and FixtureServiceImpl.assertCanViewLeagueFixtures
        // exactly: a caller who can open a public league's detail page and fixtures must also be
        // able to see its member list, otherwise a public league's page contradicts itself (real
        // fixtures/scores visible, membership hidden). The rule is the shared
        // LeagueVisibility.canView predicate rather than a hand-copied if.
        boolean isActiveMember = isLeagueMember(parsedLeagueId, actorId);
        if (!LeagueVisibility.canView(league.getLeagueType(), isAdmin(actorId), isActiveMember)) {
            throw new AuthorisationException("You must be a league member to view its members.");
        }

        List<LeagueMembership> memberships = membershipDAO.findActiveByLeague(parsedLeagueId);
        List<LeagueMemberResponseDTO> responses = new ArrayList<>();

        for (LeagueMembership membership : memberships) {
            responses.add(toMemberResponse(membership));
        }

        return responses;
    }

    @Override
    public void removeMember(String actorUserId, String leagueId, String membershipId) {
        UUID actorId = parseUserId(actorUserId);
        UUID parsedLeagueId = parseLeagueId(leagueId);
        UUID parsedMembershipId = parseMembershipId(membershipId);

        League league = requireLeague(parsedLeagueId);
        requireLeagueManager(league, actorId);

        LeagueMembership membership = membershipDAO.findById(parsedMembershipId).orElseThrow(() -> new ResourceNotFoundException("League membership not found."));

        if (!parsedLeagueId.equals(membership.getLeagueId())) {
            throw new ResourceNotFoundException("League membership not found.");
        }

        if (!Boolean.TRUE.equals(membership.getIsActive())) {
            throw new ConflictException("The league membership is not active.");
        }

        if (league.getManagerUserId() != null && league.getManagerUserId().equals(membership.getRegisteredUserId())) {

            throw new BusinessRuleException("The league manager cannot be removed.");
        }

        if (!membershipDAO.deactivateMembership(parsedMembershipId)) {
            throw new BusinessRuleException("The league membership could not be removed.");
        }

        notifyLeagueRemoval(league, membership);
    }

    @Override
    public String getLeagueCode(String actorUserId, String leagueId) {
        UUID actorId = parseUserId(actorUserId);
        UUID parsedLeagueId = parseLeagueId(leagueId);

        League league = requireLeague(parsedLeagueId);
        requireLeagueManager(league, actorId);

        if (league.getLeagueType() != LeagueType.PRIVATE) {
            throw new BusinessRuleException("Public leagues do not have private join codes.");
        }

        if (league.getLeagueCode() == null || league.getLeagueCode().isBlank()) {
            throw new ResourceNotFoundException("The league code was not found.");
        }

        return league.getLeagueCode();
    }

    private void validateCreateLeagueRequest(LeagueRequestDTO request, UUID currentUserId) {
        if (request == null) {
            throw new ValidationException("League request cannot be null.");
        }

        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        if (request.getLeagueName() == null || request.getLeagueName().isBlank()) {
            throw new ValidationException("League name is required.");
        }

        if (request.getLeagueType() == null) {
            throw new ValidationException("League type is required.");
        }

        if (request.getMaxMembers() <= 1) {
            throw new ValidationException("Maximum members must be greater than 1.");
        }
    }

    private void validateJoinLeagueRequest(JoinLeagueRequestDTO request, UUID currentUserId) {
        if (request == null) {
            throw new ValidationException("Join-league request cannot be null.");
        }

        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        if (request.getLeagueId() == null) {
            throw new ValidationException("That league could not be found. Check the join code and try again.");
        }

        if (request.getTeamId() == null) {
            throw new ValidationException("You need a fantasy team before you can join a league.");
        }
    }

    private void validatePrivateLeagueCode(League league, String suppliedCode) {
        String actualCode = league.getLeagueCode();

        if (suppliedCode == null || suppliedCode.isBlank() || actualCode == null || !actualCode.equalsIgnoreCase(suppliedCode.trim())) {
            throw new AuthorisationException("The private league code is invalid.");
        }
    }

    private League requireLeague(UUID leagueId) {
        return leagueDAO.findLeagueById(leagueId).orElseThrow(() -> new ResourceNotFoundException("League not found."));
    }

    private void requireLeagueManager(League league, UUID actorUserId) {
        if (league.getManagerUserId() == null || !actorUserId.equals(league.getManagerUserId())) {
            throw new AuthorisationException("Only the league manager may perform this action.");
        }
    }

    private UUID parseUserId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Current user ID is required.");
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Current user ID must be a valid UUID.");
        }
    }

    private UUID parseLeagueId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("League ID is required.");
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("League ID must be a valid UUID.");
        }
    }

    private UUID parseMembershipId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Membership ID is required.");
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Membership ID must be a valid UUID.");
        }
    }

    private String generateLeagueCode() {
        String code;

        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (leagueDAO.existsByLeagueCode(code));

        return code;
    }

    private LeagueMemberResponseDTO toMemberResponse(LeagueMembership membership) {

        LeagueMemberResponseDTO response = new LeagueMemberResponseDTO();
        response.setMembershipId(membership.getMembershipId());
        response.setUserId(membership.getRegisteredUserId());
        response.setUserDisplayName(userDAO.getUserById(membership.getRegisteredUserId()).map(User::getUsername).orElse("Unknown User"));
        response.setTeamId(membership.getTeamId());
        response.setTeamDisplayName(fantasyTeamDAO.getTeamById(membership.getTeamId()).map(FantasyTeam::getTeamName).orElse("Unknown Team"));
        response.setJoinDate(membership.getJoinDate());
        response.setIsActive(membership.getIsActive());
        return response;
    }

    private boolean isAdmin(UUID userId) {
        if (userId == null) {
            return false;
        }
        Optional<User> user = userDAO.getUserById(userId);
        return user.isPresent() && user.get().getRole() == UserRole.ADMINISTRATOR;
    }

    private void notifyLeagueJoin(League league, UUID newMemberUserId, FantasyTeam team) {
        try {
            String memberBody = "You joined the league \"" + league.getLeagueName() + "\".";
            notificationService.notifyLeagueMembershipEvent(newMemberUserId, league.getLeagueId(), memberBody);

            if (league.getManagerUserId() != null && !league.getManagerUserId().equals(newMemberUserId)) {
                String teamName = team != null && team.getTeamName() != null ? team.getTeamName() : "A new team";
                String managerBody = teamName + " joined your league \"" + league.getLeagueName() + "\".";
                notificationService.notifyLeagueMembershipEvent(league.getManagerUserId(), league.getLeagueId(), managerBody);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send league-join notifications for league " + league.getLeagueId(), e);
        }
    }

    private void notifyLeagueRemoval(League league, LeagueMembership membership) {
        try {
            UUID removedUserId = membership.getRegisteredUserId();
            if (removedUserId != null) {
                String removedBody = "You have been removed from the league \"" + league.getLeagueName() + "\".";
                notificationService.notifyLeagueMembershipEvent(removedUserId, league.getLeagueId(), removedBody);
            }

            if (league.getManagerUserId() != null && !league.getManagerUserId().equals(removedUserId)) {
                String managerBody = "A member was removed from your league \"" + league.getLeagueName() + "\".";
                notificationService.notifyLeagueMembershipEvent(league.getManagerUserId(), league.getLeagueId(), managerBody);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to send league-removal notifications for league " + league.getLeagueId(), e);
        }
    }

    /**
     * The manager's username, or null when there is no manager. A public league
     * never has one: it is run by the administrators, so nobody owns it.
     */
    private String managerDisplayName(League league) {
        UUID managerId = league.getManagerUserId();
        if (managerId == null) {
            return null;
        }
        return userDAO.getUserById(managerId).map(User::getUsername).orElse(null);
    }

    private LeagueResponseDTO toResponse(League league) {

        return LeagueResponseDTO.builder()
                .leagueId(league.getLeagueId())
                .managerUserId(league.getManagerUserId())
                .managerDisplayName(managerDisplayName(league))
                .leagueName(league.getLeagueName())
                .description(league.getDescription())
                .leagueType(league.getLeagueType())
                .creationDate(league.getCreationDate())
                .isActive(league.getIsActive())
                .maxMembers(league.getMaxMembers())
                .leagueCode(league.getLeagueCode())
                .status(league.getStatus())
                .startedAt(league.getStartedAt())
                .build();
    }
}