package com.vzap.trytons.league.dao;

import com.vzap.trytons.league.enums.LeagueMemberRole;
import com.vzap.trytons.league.model.League;
import com.vzap.trytons.league.model.LeagueMembership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueMembershipDAO {

    LeagueMembership createMembership(UUID leagueId, UUID userId, UUID teamId, LeagueMemberRole role);

    Optional<LeagueMembership> findById(UUID membershipId);

    List<LeagueMembership> findActiveByLeague(UUID leagueId);

    List<LeagueMembership> findActiveByUser(UUID userId);

    List<League> findLeaguesByLeague(UUID leagueId); //

    List<League> findLeaguesByUser(UUID userId); //

    boolean existsActiveByLeagueAndUser(UUID leagueId, UUID userId);

    int countActiveMembers(UUID leagueId);

    boolean deactivateMembership(UUID membershipId);

    boolean updateRole(UUID membershipId, LeagueMemberRole newRole);


}