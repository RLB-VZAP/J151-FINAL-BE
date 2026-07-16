package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.LeagueDAO;
import com.vzap.trytons.dao.LeagueMembershipDAO;
import com.vzap.trytons.dto.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.LeagueMemberResponseDTO;
import com.vzap.trytons.dto.LeagueRequestDTO;
import com.vzap.trytons.dto.LeagueResponseDTO;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.League;
import com.vzap.trytons.model.LeagueMembership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LeagueServiceImpl implements LeagueService {

    private final LeagueDAO leagueDAO;
    private final LeagueMembershipDAO membershipDAO;
    private final FantasyTeamDAO fantasyTeamDAO;

    /*
     * The @Inject annotation is the important CDI fix.
     * It allows GlassFish/Weld to construct this service.
     */
    @Inject
    public LeagueServiceImpl(
            LeagueDAO leagueDAO,
            LeagueMembershipDAO membershipDAO,
            FantasyTeamDAO fantasyTeamDAO) {

        this.leagueDAO = leagueDAO;
        this.membershipDAO = membershipDAO;
        this.fantasyTeamDAO = fantasyTeamDAO;
    }

    @Override
    public LeagueResponseDTO createLeague(
            LeagueRequestDTO request,
            UUID currentUserId) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "League request cannot be null."
            );
        }

        if (currentUserId == null) {
            throw new IllegalArgumentException(
                    "Current user ID is required."
            );
        }

        if (request.getLeagueName() == null
                || request.getLeagueName().isBlank()) {

            throw new IllegalArgumentException(
                    "League name is required."
            );
        }

        if (request.getLeagueType() == null) {
            throw new IllegalArgumentException(
                    "League type is required."
            );
        }

        if (request.getMaxMembers() <= 1) {
            throw new IllegalArgumentException(
                    "Maximum members must be greater than 1."
            );
        }

        String leagueName =
                request.getLeagueName().trim();

        if (leagueDAO
                .findLeagueByName(leagueName)
                .isPresent()) {

            throw new ConflictException(
                    "A league with this name already exists."
            );
        }

        List<FantasyTeam> ownedTeams =
                fantasyTeamDAO.findTeamsByOwner(
                        currentUserId
                );

        if (ownedTeams == null || ownedTeams.isEmpty()) {
            throw new ConflictException(
                    "You must create a fantasy team "
                            + "before creating a league."
            );
        }

        FantasyTeam team = ownedTeams.get(0);

        League league = new League();
        league.setLeagueId(UUID.randomUUID());
        league.setLeagueName(leagueName);
        league.setDescription(request.getDescription());
        league.setLeagueType(request.getLeagueType());
        league.setMaxMembers(request.getMaxMembers());
        league.setIsActive(true);

        if (request.getLeagueType() == LeagueType.PRIVATE) {
            league.setLeagueCode(generateLeagueCode());
        } else {
            league.setLeagueCode(null);
        }

        League savedLeague =
                leagueDAO.createLeague(league);

        if (savedLeague == null
                || savedLeague.getLeagueId() == null) {

            throw new IllegalStateException(
                    "The league could not be created."
            );
        }

        /*
         * The creator must first become an active member.
         * The league manager is then assigned afterwards.
         */
        membershipDAO.createMembership(
                savedLeague.getLeagueId(),
                currentUserId,
                team.getTeamId()
        );

        leagueDAO.assignManager(
                savedLeague.getLeagueId(),
                currentUserId
        );

        LeagueResponseDTO response =
                toResponse(savedLeague);

        response.setManagerUserId(currentUserId);

        return response;
    }

    @Override
    public LeagueResponseDTO getLeague(
            UUID leagueId,
            UUID currentUserId) {

        if (leagueId == null) {
            throw new IllegalArgumentException(
                    "League ID is required."
            );
        }

        League league = requireLeague(leagueId);

        if (league.getLeagueType() == LeagueType.PRIVATE
                && !isLeagueMember(
                leagueId,
                currentUserId
        )) {

            throw new ConflictException(
                    "You are not permitted to view "
                            + "this private league."
            );
        }

        return toResponse(league);
    }

    @Override
    public boolean isLeagueMember(
            UUID leagueId,
            UUID userId) {

        if (leagueId == null || userId == null) {
            return false;
        }

        return membershipDAO
                .existsActiveByLeagueAndUser(
                        leagueId,
                        userId
                );
    }

    @Override
    public List<LeagueResponseDTO> getAllLeagues(
            UUID currentUserId) {

        List<League> leagues =
                leagueDAO.findAllLeagues();

        List<LeagueResponseDTO> responses =
                new ArrayList<>();

        if (leagues == null) {
            return responses;
        }

        for (League league : leagues) {

            if (league == null) {
                continue;
            }

            boolean publicLeague =
                    league.getLeagueType()
                            == LeagueType.PUBLIC;

            boolean member =
                    currentUserId != null
                            && isLeagueMember(
                            league.getLeagueId(),
                            currentUserId
                    );

            if (publicLeague || member) {
                responses.add(toResponse(league));
            }
        }

        return responses;
    }

    @Override
    public JoinLeagueResponseDTO joinLeague(
            JoinLeagueRequestDTO request,
            UUID currentUserId) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Join-league request cannot be null."
            );
        }

        if (currentUserId == null) {
            throw new IllegalArgumentException(
                    "Current user ID is required."
            );
        }

        UUID leagueId = request.getLeagueId();
        UUID teamId = request.getTeamId();

        if (leagueId == null) {
            throw new IllegalArgumentException(
                    "League ID is required."
            );
        }

        if (teamId == null) {
            throw new IllegalArgumentException(
                    "Team ID is required."
            );
        }

        League league = requireLeague(leagueId);

        FantasyTeam team = fantasyTeamDAO
                .getTeamById(teamId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Fantasy team not found."
                        )
                );

        if (team.getOwnerUserId() == null
                || !currentUserId.equals(
                team.getOwnerUserId()
        )) {

            throw new ConflictException(
                    "The selected fantasy team does not "
                            + "belong to the current user."
            );
        }

        if (membershipDAO
                .existsActiveByLeagueAndUser(
                        leagueId,
                        currentUserId
                )) {

            throw new ConflictException(
                    "You are already a member of this league."
            );
        }

        if (league.getLeagueType() == LeagueType.PRIVATE) {

            String suppliedCode =
                    request.getLeagueCode();

            String actualCode =
                    league.getLeagueCode();

            if (suppliedCode == null
                    || suppliedCode.isBlank()
                    || actualCode == null
                    || !actualCode.equalsIgnoreCase(
                    suppliedCode.trim()
            )) {

                throw new ConflictException(
                        "The private-league code is invalid."
                );
            }
        }

        int activeMemberCount =
                membershipDAO.countActiveMembers(
                        leagueId
                );

        if (activeMemberCount
                >= league.getMaxMembers()) {

            throw new ConflictException(
                    "This league is already full."
            );
        }

        LeagueMembership membership =
                membershipDAO.createMembership(
                        leagueId,
                        currentUserId,
                        teamId
                );

        if (membership == null
                || membership.getMembershipId() == null) {

            throw new IllegalStateException(
                    "The league membership could "
                            + "not be created."
            );
        }

        JoinLeagueResponseDTO response =
                new JoinLeagueResponseDTO();

        response.setLeagueId(
                league.getLeagueId()
        );

        response.setLeagueName(
                league.getLeagueName()
        );

        response.setMembershipId(
                membership.getMembershipId()
        );

        response.setMessage(
                "Joined the league successfully."
        );

        return response;
    }

    /*
     * These three methods were added to the merged interface.
     *
     * They are kept deployment-safe here because the matching
     * LeagueMembershipDAO retrieval/removal contracts have not
     * yet been confirmed in the merged project.
     */

    @Override
    public List<LeagueMemberResponseDTO> listMembers(
            String actorUserId,
            String leagueId) {

        UUID actorId =
                parseUuid(actorUserId, "actorUserId");

        UUID parsedLeagueId =
                parseUuid(leagueId, "leagueId");

        requireLeague(parsedLeagueId);

        if (!isLeagueMember(
                parsedLeagueId,
                actorId
        )) {

            throw new ConflictException(
                    "You must be a league member "
                            + "to view its members."
            );
        }

        List<LeagueMembership> memberships =
                membershipDAO.findActiveByLeague(parsedLeagueId);

        List<LeagueMemberResponseDTO> response = new ArrayList<>();

        if (memberships != null) {

            for (LeagueMembership membership : memberships) {
                response.add(toMemberResponse(membership));
            }
        }
        return new ArrayList<>();
    }

    private LeagueMemberResponseDTO toMemberResponse(LeagueMembership membership) {
        LeagueMemberResponseDTO dto = new LeagueMemberResponseDTO();
        dto.setMembershipId(membership.getMembershipId());
        dto.setUserId(membership.getRegisteredUserId()); // rename: registeredUserId -> userId
        dto.setTeamId(membership.getTeamId());
        dto.setJoinDate(membership.getJoinDate());
        dto.setIsActive(membership.getIsActive());
        return dto;
    }

    @Override
    public void removeMember(
            String actorUserId,
            String leagueId,
            String membershipId) {

        UUID actorId =
                parseUuid(actorUserId, "actorUserId");

        UUID parsedLeagueId =
                parseUuid(leagueId, "leagueId");

        UUID parsedMembershipId = parseUuid(
                membershipId,
                "membershipId"
        );

        League league =
                requireLeague(parsedLeagueId);

        requireLeagueManager(
                league,
                actorId
        );

        LeagueMembership membership = membershipDAO
                .findById(parsedMembershipId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "League membership not found."
                        )
                );

        if (!parsedLeagueId.equals(membership.getLeagueId())) {
            throw new ResourceNotFoundException(
                    "League membership not found."
            );
        }

        if (!membershipDAO.deactivateMembership(parsedMembershipId)) {
            throw new IllegalStateException(
                    "The league membership could not be removed."
            );
        }
    }

    @Override
    public String getLeagueCode(
            String actorUserId,
            String leagueId) {

        UUID actorId =
                parseUuid(actorUserId, "actorUserId");

        UUID parsedLeagueId =
                parseUuid(leagueId, "leagueId");

        League league =
                requireLeague(parsedLeagueId);

        requireLeagueManager(
                league,
                actorId
        );

        if (league.getLeagueType()
                != LeagueType.PRIVATE) {

            throw new ConflictException(
                    "Public leagues do not have "
                            + "private join codes."
            );
        }

        if (league.getLeagueCode() == null
                || league.getLeagueCode().isBlank()) {

            throw new ResourceNotFoundException(
                    "The league code was not found."
            );
        }

        return league.getLeagueCode();
    }

    private League requireLeague(UUID leagueId) {

        return leagueDAO
                .findLeagueById(leagueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "League not found."
                        )
                );
    }

    private void requireLeagueManager(
            League league,
            UUID actorUserId) {

        if (league.getManagerUserId() == null
                || !actorUserId.equals(
                league.getManagerUserId()
        )) {

            throw new AuthorisationException(
                    "Only the league manager may "
                            + "perform this action."
            );
        }
    }

    private UUID parseUuid(
            String value,
            String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        try {
            return UUID.fromString(value.trim());

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    fieldName
                            + " must contain a valid UUID.",
                    exception
            );
        }
    }

    private String generateLeagueCode() {

        String code;

        do {
            code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();

        } while (leagueDAO.existsByLeagueCode(code));

        return code;
    }

    private LeagueResponseDTO toResponse(
            League league) {

        LeagueResponseDTO response =
                new LeagueResponseDTO();

        response.setLeagueId(
                league.getLeagueId()
        );

        if (league.getManagerUserId() != null) {
            response.setManagerUserId(
                    league.getManagerUserId()
            );
        }

        response.setLeagueName(
                league.getLeagueName()
        );

        response.setDescription(
                league.getDescription()
        );

        response.setLeagueType(
                league.getLeagueType()
        );

        response.setLeagueCode(
                league.getLeagueCode()
        );

        response.setCreationDate(
                league.getCreationDate()
        );

        response.setIsActive(
                league.getIsActive()
        );

        response.setMaxMembers(
                league.getMaxMembers()
        );

        return response;
    }
}