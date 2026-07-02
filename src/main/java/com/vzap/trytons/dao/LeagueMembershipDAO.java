package com.vzap.trytons.dao;

import com.vzap.trytons.dto.LeagueResponseDTO;
import com.vzap.trytons.enums.LeagueMemberRole;
import com.vzap.trytons.model.LeagueMembership;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueMembershipDAO {

    UUID createMembership(UUID leagueId, UUID userId, UUID teamId, LeagueMemberRole role);

    Optional<LeagueMembership> findById(UUID membershipId);

    List<LeagueMembership> findActiveByLeague(UUID leagueId);

    List<LeagueMembership> findActiveByUser(UUID userId);

    List<LeagueResponseDTO> findResponsesByLeague(UUID leagueId);

    List<LeagueResponseDTO> findResponsesByUser(UUID userId);

    boolean existsActiveByLeagueAndUser(UUID leagueId, UUID userId);

    int countActiveMembers(UUID leagueId);

    boolean deactivateMembership(UUID membershipId);

    boolean updateRole(UUID membershipId, LeagueMemberRole newRole);
}