package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.LeagueDAO;
import com.vzap.trytons.dao.LeagueMembershipDAO;
import com.vzap.trytons.dto.JoinLeagueRequestDTO;
import com.vzap.trytons.dto.JoinLeagueResponseDTO;
import com.vzap.trytons.dto.LeagueRequestDTO;
import com.vzap.trytons.dto.LeagueResponseDTO;
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
                throw new ConflictException("Missing or invalid league code.");
            }
        }
        if(membershipDAO.countActiveMembers(leagueId) >= league.getMaxMembers()){
            throw new ConflictException("This league is full.");
        }
        LeagueMembership membership = membershipDAO.createMembership(leagueId,currentUserId,teamId);
        JoinLeagueResponseDTO response = new JoinLeagueResponseDTO();
        response.setLeagueId(league.getLeagueId());
        response.setLeagueName(league.getLeagueName());
        response.setMembershipId(membership.getMembershipId());
        response.setMessage("Joined the league successfully.");
        return  response;
    }
}
