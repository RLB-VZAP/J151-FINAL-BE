package com.vzap.trytons.service;

import com.vzap.trytons.dao.FantasyTeamDAO;
import com.vzap.trytons.dao.LeagueDAO;
import com.vzap.trytons.dao.LeagueMembershipDAO;
import com.vzap.trytons.dto.LeagueRequestDTO;
import com.vzap.trytons.dto.LeagueResponseDTO;

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
    public LeagueResponseDTO createLeague(LeagueResponseDTO createLeague, LeagueRequestDTO request, UUID currentUserId) {

        if (request == null){
            request.getLeagueName();
        }
        return null;
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
}
