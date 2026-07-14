package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.LeagueDAO;
import com.vzap.trytons.dao.LeagueMembershipDAO;
import com.vzap.trytons.dto.*;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.*;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.League;
import com.vzap.trytons.model.LeagueMembership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
@AllArgsConstructor
@ApplicationScoped
public class LeagueServiceImpl implements LeagueService{
    @Inject
    private LeagueDAO leagueDAO;
    @Inject
    private LeagueMembershipDAO membershipDAO;
    @Inject
    private FantasyTeamDAO fantasyTeamDAO;
    @Inject
    private NotificationService notificationService;

private static final Logger LOG = Logger.getLogger(LeagueServiceImpl.class.getName());

    @Override
    public LeagueResponseDTO createLeague(LeagueRequestDTO request, UUID currentUserId){

        if (request == null) {
            throw new IllegalArgumentException("League request cannot be null.");
        }

        if (request.getLeagueName() == null ||
                request.getLeagueName().isBlank()) {

            throw new IllegalArgumentException("League name is required.");
        }

        if (request.getMaxMembers() <= 1){
            throw new  IllegalArgumentException("maxMembers must be greater than 1.");
        }

        if (leagueDAO.findLeagueByName(request.getLeagueName()).isPresent()) {
            throw new IllegalArgumentException("League name already exists.");
        }


        List<FantasyTeam> teams = fantasyTeamDAO.findTeamsByOwner(currentUserId);

        if (teams.isEmpty()) {
            throw new IllegalArgumentException("You must create a fantasy team before creating a league.");
        }

        FantasyTeam team = teams.get(0);

        League league = new League();

        league.setLeagueId(UUID.randomUUID());
        league.setLeagueName(request.getLeagueName());
        league.setDescription(request.getDescription());
        league.setLeagueType(request.getLeagueType());
        league.setMaxMembers(request.getMaxMembers());

        if (request.getLeagueType() == LeagueType.PRIVATE) {
            league.setLeagueCode(generateLeagueCode());
        }

        League savedLeague = leagueDAO.createLeague(league);

        membershipDAO.createMembership(savedLeague.getLeagueId(), currentUserId, team.getTeamId());

        leagueDAO.assignManager(savedLeague.getLeagueId(), currentUserId);

       LeagueResponseDTO response = new LeagueResponseDTO();
        response.setLeagueId(savedLeague.getLeagueId());
        response.setManagerUserId(currentUserId);
        response.setLeagueName(savedLeague.getLeagueName());
        response.setLeagueType(savedLeague.getLeagueType());
        response.setLeagueCode(savedLeague.getLeagueCode());
        response.setDescription(savedLeague.getDescription());
        response.setCreationDate(savedLeague.getCreationDate());
        response.setIsActive(savedLeague.getIsActive());
        response.setMaxMembers(savedLeague.getMaxMembers());

        return response;
    }

    private String generateLeagueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (leagueDAO.existsByLeagueCode(code));
        return code;
    }

    @Override
    public LeagueResponseDTO getLeague(UUID leagueId, UUID currentUserId) {

        Optional<League> leagueOptional = leagueDAO.findLeagueById(leagueId);

        if (leagueOptional.isEmpty()){
            throw new IllegalArgumentException("League not found");
        }

        League league2 = leagueOptional.get();

        if (league2.getLeagueType() == LeagueType.PRIVATE) {

            if (!isLeagueMember(leagueId, currentUserId)) {
                throw new IllegalArgumentException(
                        "You are not allowed to view this private league.");
            }
        }

        LeagueResponseDTO response = new LeagueResponseDTO();
        response.setLeagueId(league2.getLeagueId());

        if (league2.getManager() != null) {
            response.setManagerUserId(league2.getManager().getUserId());
        }

        response.setLeagueName(league2.getLeagueName());
        response.setDescription(league2.getDescription());
        response.setLeagueType(league2.getLeagueType());
        response.setLeagueCode(league2.getLeagueCode());
        response.setCreationDate(league2.getCreationDate());
        response.setIsActive(league2.getIsActive());
        response.setMaxMembers(league2.getMaxMembers());

        return response;
    }

    @Override
    public boolean isLeagueMember(UUID leagueId, UUID userId) {

        return membershipDAO.existsActiveByLeagueAndUser(leagueId, userId);
    }

    @Override
    public List<LeagueResponseDTO> getAllLeagues(UUID currentUserId){

        List<League> leagues = leagueDAO.findAllLeagues();

        List<LeagueResponseDTO> visibleLeagues = new ArrayList<>();

        for (League league : leagues) {

            if (league.getLeagueType() == LeagueType.PUBLIC || isLeagueMember(league.getLeagueId(), currentUserId)){

                LeagueResponseDTO response = new LeagueResponseDTO();

                response.setLeagueId(league.getLeagueId());

                if (league.getManager() != null) {
                    response.setManagerUserId(league.getManager().getUserId());
                }

                response.setLeagueName(league.getLeagueName());
                response.setDescription(league.getDescription());
                response.setLeagueType(league.getLeagueType());
                response.setLeagueCode(league.getLeagueCode());
                response.setCreationDate(league.getCreationDate());
                response.setIsActive(league.getIsActive());
                response.setMaxMembers(league.getMaxMembers());

                visibleLeagues.add(response);

            }
        }
        return visibleLeagues;
    }

    @Override
    public JoinLeagueResponseDTO joinLeague(JoinLeagueRequestDTO request, UUID currentUserId) {
        UUID leagueId = request.getLeagueId();
        UUID teamId = request.getTeamId();
        League league = leagueDAO.findLeagueById(leagueId).orElseThrow(() -> new ResourceNotFoundException("League not found"));
        FantasyTeam team = fantasyTeamDAO.getTeamById(teamId).orElseThrow(() -> new ResourceNotFoundException("Team not found"));
        if(!team.getOwner().getUserId().equals(currentUserId)){
            throw new ConflictException("Team does not belong to the current user.");
        }
        if(membershipDAO.existsActiveByLeagueAndUser(leagueId, currentUserId)){
            throw new ConflictException("User already a member of this league.");
        }
        if(league.getLeagueType() == LeagueType.PRIVATE){
            String leagueCode = request.getLeagueCode();
            if(leagueCode == null || !leagueCode.equals(league.getLeagueCode())){
                throw new ValidationException("Missing or invalid league code.");
            }
        }
        if(membershipDAO.countActiveMembers(leagueId) >= league.getMaxMembers()){
            throw new BusinessRuleException("This league is full.");
        }

        LeagueMembership membership = membershipDAO.createMembership(leagueId,currentUserId,teamId);

        if (league.getManager() != null && !league.getManager().getUserId().equals(currentUserId)) {
            String joiningUserDisplayName = team.getOwner() != null ? team.getOwner().getUsername() : "A new member";
            String body = joiningUserDisplayName + " joined " + league.getLeagueName() + ".";
            notificationService.notifyLeagueMembershipEvent(league.getManager().getUserId(), league.getLeagueId(), body);
        }

        JoinLeagueResponseDTO response = new JoinLeagueResponseDTO();
        response.setLeagueId(league.getLeagueId());
        response.setLeagueName(league.getLeagueName());
        response.setMembershipId(membership.getMembershipId());
        response.setMessage("Joined the league successfully.");
        return  response;
    }

    private UUID parseUuid(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid " + fieldName + " format.");
        }
    }

    @Override
    public List<LeagueMemberResponseDTO> listMembers(String actorUserId, String leagueId) {
        UUID actorId = parseUuid(actorUserId, "actorUserId");
        UUID leagueUuid = parseUuid(leagueId, "leagueId");

        League league = leagueDAO.findLeagueById(leagueUuid)
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));

        boolean isManager = league.getManager() != null
                && league.getManager().getUserId().equals(actorId);
        boolean isMember = membershipDAO.existsActiveByLeagueAndUser(leagueUuid, actorId);

        if (league.getLeagueType() == LeagueType.PRIVATE && !isMember && !isManager) {
            throw new AuthorisationException("You are not allowed to view this league's members.");
        }

        List<LeagueMembership> memberships = membershipDAO.findActiveByLeague(leagueUuid);

        List<LeagueMemberResponseDTO> result = new ArrayList<>();
        for (LeagueMembership membership : memberships) {
            LeagueMemberResponseDTO dto = new LeagueMemberResponseDTO();
            dto.setMembershipId(membership.getMembershipId());
            dto.setUserId(membership.getRegisteredUser().getUserId());
            dto.setTeamId(membership.getFantasyTeam().getTeamId());
            dto.setJoinDate(membership.getJoinDate());
            dto.setIsActive(membership.getIsActive());
            result.add(dto);
        }
        return result;
    }

    @Override
    public void removeMember(String actorUserId, String leagueId, String membershipId) {
        UUID actorId = parseUuid(actorUserId, "actorUserId");
        UUID leagueUuid = parseUuid(leagueId, "leagueId");
        UUID membershipUuid = parseUuid(membershipId, "membershipId");

        League league = leagueDAO.findLeagueById(leagueUuid)
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));

        boolean isManager = league.getManager() != null
                && league.getManager().getUserId().equals(actorId);
        if (!isManager) {
            throw new AuthorisationException("Only the league manager can remove members.");
        }

        LeagueMembership membership = membershipDAO.findById(membershipUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));

        if (!membership.getLeague().getLeagueId().equals(leagueUuid)) {
            throw new ResourceNotFoundException("Membership not found in this league.");
        }

        boolean removed = membershipDAO.deactivateMembership(membershipUuid);
        if (!removed) {
            throw new ResourceNotFoundException("Membership not found.");
        }
    }

    @Override
    public String getLeagueCode(String actorUserId, String leagueId) {
        UUID actorId = parseUuid(actorUserId, "actorUserId");
        UUID leagueUuid = parseUuid(leagueId, "leagueId");

        League league = leagueDAO.findLeagueById(leagueUuid)
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));

        boolean isManager = league.getManager() != null
                && league.getManager().getUserId().equals(actorId);
        if (!isManager) {
            throw new AuthorisationException("Only the league manager can view the join code.");
        }

        return league.getLeagueCode();
    }
}
