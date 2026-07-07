package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.LeagueDAO;
import com.vzap.trytons.dao.LeagueMembershipDAO;
import com.vzap.trytons.dto.LeagueRequestDTO;
import com.vzap.trytons.dto.LeagueResponseDTO;
import com.vzap.trytons.enums.LeagueMemberRole;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.model.League;

import java.util.List;
import java.util.UUID;

public class LeagueServiceImpl implements LeagueService{

    private final LeagueDAO leagueDAO;
    private final LeagueMembershipDAO membershipDAO;
    private final FantasyTeamDAO fantasyTeamDAO;

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

        membershipDAO.createMembership( savedLeague.getLeagueId(), currentUserId, role);

//        List<FantasyTeam> teams = fantasyTeamDAO.findTeamsByOwner(currentUserId);
//
//        if (teams.isEmpty()) {
//            throw new IllegalArgumentException(
//                    "You must create a fantasy team before creating a league.");
//        }
//
//        League league = new League();
//
//        league.setLeagueName(request.getLeagueName());
//        league.setLeagueType(request.getLeagueType());
//
//        if (request.getLeagueType() == LeagueType.PRIVATE) {
//            league.setLeagueManagerId(currentUserId);
//        }
//
//        League savedLeague = leagueDAO.createLeague(league);

        return new LeagueResponseDTO(savedLeague);
    }

    @Override
    public LeagueResponseDTO getLeague(UUID leagueId, UUID currentUserId) {
        return null;
    }

    @Override
    public void joinLeague(UUID leagueId, UUID currentUserId, UUID fantasyTeamId) {

    }

    @Override
    public boolean isLeagueMember(UUID leagueId, UUID userId) {
        return false;
    }

    @Override
    public List<LeagueResponseDTO> getAllLeagues(UUID currentUserId) {
        return List.of();
    }
}
