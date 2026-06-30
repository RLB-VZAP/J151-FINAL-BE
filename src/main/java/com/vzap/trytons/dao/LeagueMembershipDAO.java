package com.vzap.trytons.dao;

import com.vzap.trytons.enums.LeagueMemberRole;
import com.vzap.trytons.model.LeagueMembership;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueMembershipDAO {

    UUID createMembership(UUID leagueId, UUID userId, UUID teamId, LeagueMemberRole role) throws SQLException;

    Optional<LeagueMembership> findById(UUID membershipId) throws SQLException;

    List<LeagueMembership> findActiveByLeague(UUID leagueId) throws SQLException;

    List<LeagueMembership> findActiveByUser(UUID userId) throws SQLException;

    boolean existsActiveByLeagueAndUser(UUID leagueId, UUID userId) throws SQLException;

    int countActiveMembers(UUID leagueId) throws SQLException;

    boolean deactivateMembership(UUID membershipId) throws SQLException;

    boolean updateRole(UUID membershipId, LeagueMemberRole newRole) throws SQLException;
}