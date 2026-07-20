package com.vzap.trytons.service.league;

import com.vzap.trytons.dao.auth.RegisteredUserDAO;
import com.vzap.trytons.dao.fantasyteam.FantasyTeamDAO;
import com.vzap.trytons.dao.league.LeagueDAO;
import com.vzap.trytons.dao.league.LeagueMembershipDAO;
import com.vzap.trytons.dto.league.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.league.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.league.LeagueMemberResponseDTO;
import com.vzap.trytons.dto.league.LeagueRequestDTO;
import com.vzap.trytons.dto.league.LeagueResponseDTO;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.BusinessRuleException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.auth.RegisteredUser;
import com.vzap.trytons.model.fantasyteam.FantasyTeam;
import com.vzap.trytons.model.league.League;
import com.vzap.trytons.model.league.LeagueMembership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LeagueServiceImpl implements LeagueService {

    @Inject
    private LeagueDAO leagueDAO;

    @Inject
    private LeagueMembershipDAO membershipDAO;

    @Inject
    private FantasyTeamDAO fantasyTeamDAO;

    @Inject
    private RegisteredUserDAO registeredUserDAO;

    @Override
    public LeagueResponseDTO createLeague(LeagueRequestDTO request, UUID currentUserId) {
        validateCreateLeagueRequest(request, currentUserId);

        String leagueName = request.getLeagueName().trim();

        if (leagueDAO.findLeagueByName(leagueName).isPresent()) {
            throw new ConflictException("A league with this name already exists.");
        }

        FantasyTeam team = fantasyTeamDAO.getTeamByOwner(currentUserId)
                .orElseThrow(() -> new BusinessRuleException("You must create a fantasy team before creating a league."));

        League league = new League();
        league.setLeagueId(UUID.randomUUID());
        league.setLeagueName(leagueName);
        league.setDescription(request.getDescription());
        league.setLeagueType(request.getLeagueType());
        league.setMaxMembers(request.getMaxMembers());
        league.setIsActive(true);

        if (request.getLeagueType() == LeagueType.PRIVATE) {
            league.setLeagueCode(generateLeagueCode());
        }

        League savedLeague = leagueDAO.createLeague(league);

        if (savedLeague == null || savedLeague.getLeagueId() == null) {
            throw new BusinessRuleException("The league could not be created.");
        }

        LeagueMembership managerMembership = membershipDAO.createMembership(
                savedLeague.getLeagueId(), currentUserId, team.getTeamId());

        if (managerMembership == null || managerMembership.getMembershipId() == null) {
            throw new BusinessRuleException("The league manager membership could not be created.");
        }

        boolean managerAssigned = leagueDAO.assignManager(savedLeague.getLeagueId(), currentUserId);

        if (!managerAssigned) {
            throw new BusinessRuleException("The league manager could not be assigned.");
        }

        return toResponse(requireLeague(savedLeague.getLeagueId()));
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

        if (league.getLeagueType() == LeagueType.PRIVATE && !isLeagueMember(leagueId, currentUserId)) {
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

        List<League> leagues = leagueDAO.findAllLeagues();
        List<LeagueResponseDTO> responses = new ArrayList<>();

        for (League league : leagues) {
            if (league.getLeagueType() == LeagueType.PUBLIC
                    || isLeagueMember(league.getLeagueId(), currentUserId)) {

                responses.add(toResponse(league));
            }
        }

        return responses;
    }

    @Override
    public List<LeagueResponseDTO> getMyLeagues(UUID currentUserId) {
        if (currentUserId == null) {
            throw new ValidationException("Current user ID is required.");
        }

        List<League> leagues = leagueDAO.findAllLeagues();
        List<LeagueResponseDTO> responses = new ArrayList<>();

        for (League league : leagues) {
            if (isLeagueMember(league.getLeagueId(), currentUserId)) {
                responses.add(toResponse(league));
            }
        }

        return responses;
    }

    @Override
    public JoinLeagueResponseDTO joinLeague(JoinLeagueRequestDTO request, UUID currentUserId) {
        validateJoinLeagueRequest(request, currentUserId);

        League league = requireLeague(request.getLeagueId());

        FantasyTeam team = fantasyTeamDAO.getTeamById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found."));

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

        JoinLeagueResponseDTO response = new JoinLeagueResponseDTO();
        response.setLeagueId(league.getLeagueId());
        response.setLeagueName(league.getLeagueName());
        response.setMembershipId(membership.getMembershipId());
        response.setMessage("Joined the league successfully.");

        return response;
    }

    @Override
    public List<LeagueMemberResponseDTO> listMembers(String actorUserId, String leagueId) {
        UUID actorId = parseUserId(actorUserId);
        UUID parsedLeagueId = parseLeagueId(leagueId);

        requireLeague(parsedLeagueId);

        if (!isLeagueMember(parsedLeagueId, actorId)) {
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

        LeagueMembership membership = membershipDAO.findById(parsedMembershipId)
                .orElseThrow(() -> new ResourceNotFoundException("League membership not found."));

        if (!parsedLeagueId.equals(membership.getLeagueId())) {
            throw new ResourceNotFoundException("League membership not found.");
        }

        if (!Boolean.TRUE.equals(membership.getIsActive())) {
            throw new ConflictException("The league membership is not active.");
        }

        if (league.getManagerUserId() != null
                && league.getManagerUserId().equals(membership.getRegisteredUserId())) {

            throw new BusinessRuleException("The league manager cannot be removed.");
        }

        if (!membershipDAO.deactivateMembership(parsedMembershipId)) {
            throw new BusinessRuleException("The league membership could not be removed.");
        }
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
            throw new ValidationException("League ID is required.");
        }

        if (request.getTeamId() == null) {
            throw new ValidationException("Team ID is required.");
        }
    }

    private void validatePrivateLeagueCode(League league, String suppliedCode) {
        String actualCode = league.getLeagueCode();

        if (suppliedCode == null || suppliedCode.isBlank()
                || actualCode == null
                || !actualCode.equalsIgnoreCase(suppliedCode.trim())) {

            throw new AuthorisationException("The private league code is invalid.");
        }
    }

    private League requireLeague(UUID leagueId) {
        return leagueDAO.findLeagueById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found."));
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

        String teamDisplayName = fantasyTeamDAO.getTeamById(membership.getTeamId())
                .map(FantasyTeam::getTeamName)
                .orElse("Unknown Team");
        response.setTeamDisplayName(teamDisplayName);

        String userDisplayName = registeredUserDAO.getRegisteredUserById(membership.getRegisteredUserId())
                .map(RegisteredUser::getUsername)
                .orElse("Unknown User");
        response.setUserDisplayName(userDisplayName);

        response.setJoinDate(membership.getJoinDate());
        response.setIsActive(membership.getIsActive());
        return response;
    }

    private LeagueResponseDTO toResponse(League league) {

        LeagueResponseDTO response = new LeagueResponseDTO();
        response.setLeagueId(league.getLeagueId());
        response.setManagerUserId(league.getManagerUserId());
        response.setLeagueName(league.getLeagueName());
        response.setDescription(league.getDescription());
        response.setLeagueType(league.getLeagueType());
        response.setCreationDate(league.getCreationDate());
        response.setIsActive(league.getIsActive());
        response.setMaxMembers(league.getMaxMembers());
        response.setLeagueCode(league.getLeagueCode());
        return response;
    }
}