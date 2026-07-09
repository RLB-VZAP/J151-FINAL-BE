package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.LeagueDAO;
import com.vzap.trytons.dao.LeagueMembershipDAO;
import com.vzap.trytons.dto.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.LeagueRequestDTO;
import com.vzap.trytons.dto.LeagueResponseDTO;
import com.vzap.trytons.enums.LeagueMemberRole;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.League;
import com.vzap.trytons.model.LeagueMembership;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.logging.Logger;

@ApplicationScoped
public class LeagueServiceImpl implements LeagueService{
@Inject
    private final LeagueDAO leagueDAO;
@Inject
    private final LeagueMembershipDAO membershipDAO;
@Inject
    private final FantasyTeamDAO fantasyTeamDAO;

private static final Logger LOG = Logger.getLogger(LeagueServiceImpl.class.getName());

    public LeagueServiceImpl(LeagueDAO leagueDAO, LeagueMembershipDAO membershipDAO,  FantasyTeamDAO fantasyTeamDAO) {
        this.leagueDAO = leagueDAO;
        this.membershipDAO = membershipDAO;
        this.fantasyTeamDAO = fantasyTeamDAO;
    }

    @Override
    public LeagueResponseDTO createLeague(LeagueRequestDTO request, UUID currentUserId){

        if (request == null) {
            throw new IllegalArgumentException("League request cannot be null.");
        }

        if (request.getLeagueName() == null ||
                request.getLeagueName().isBlank()) {

            throw new IllegalArgumentException("League name is required.");
        }

        if (leagueDAO.findLeagueByName(request.getLeagueName()).isPresent()) {
            throw new IllegalArgumentException("League name already exists.");
        }

        League league = new League();

        league.setLeagueName(request.getLeagueName());
        league.setLeagueType(request.getLeagueType());

        League savedLeague = leagueDAO.createLeague(league);

        LeagueMemberRole role;

        if (request.getLeagueType() == LeagueType.PRIVATE) {
            role = LeagueMemberRole.MANAGER;
        } else {
            role = LeagueMemberRole.MEMBER;
        }

        List<FantasyTeam> teams = fantasyTeamDAO.findTeamsByOwner(currentUserId);

        if (teams.isEmpty()) {
            throw new IllegalArgumentException(
                    "You must create a fantasy team before creating a league.");
        }

        FantasyTeam team = teams.get(0);

        membershipDAO.createMembership(savedLeague.getLeagueId(), currentUserId, team.getTeamId(), role);

        LeagueResponseDTO response = new LeagueResponseDTO();
        response.setLeagueId(savedLeague.getLeagueId());
        response.setLeagueName(savedLeague.getLeagueName());
        response.setLeagueType(savedLeague.getLeagueType());

        return response;
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
        response.setLeagueName(league2.getLeagueName());
        response.setLeagueType(league2.getLeagueType());

        return response;
    }

    @Override
    public void joinLeague(UUID leagueId, UUID currentUserId, UUID fantasyTeamId) {

        if (isLeagueMember(leagueId, currentUserId)) {
            throw new IllegalArgumentException("You are already a member of this league");
        }
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
                throw new ConflictException("Missing or invalid league code.");
            }
        }
        if(membershipDAO.countActiveMembers(leagueId) >= league.getMaxMembers()){
            throw new ConflictException("This league is full.");
        }
        LeagueMembership membership = membershipDAO.createMembership(leagueId,currentUserId,teamId,LeagueMemberRole.MEMBER);
        JoinLeagueResponseDTO response = new JoinLeagueResponseDTO();
        response.setLeagueId(league.getLeagueId());
        response.setLeagueName(league.getLeagueName());
        response.setMembershipId(membership.getMembershipId());
        response.setMessage("Joined the league successfully.");
        return  response;
    }
}
