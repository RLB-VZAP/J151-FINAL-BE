package com.vzap.trytons.dao;

import com.vzap.trytons.dto.CreateLeagueRequest;
import com.vzap.trytons.dto.LeagueCodeResponse;
import com.vzap.trytons.dto.LeagueResponse;
import com.vzap.trytons.model.League;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueDAO {

    UUID createLeague(CreateLeagueRequest request, UUID managerId, String leagueCode) throws SQLException;

    Optional<League> findById(UUID leagueId) throws SQLException;

    List<League> findAllActive() throws SQLException;

    Optional<LeagueResponse> findResponseById(UUID leagueId) throws SQLException;

    List<LeagueResponse> findResponsesByManager(UUID managerId) throws SQLException;

    boolean existsByLeagueCode(String leagueCode) throws SQLException;

    Optional<UUID> findLeagueIdByCode(String leagueCode) throws SQLException;

    Optional<LeagueCodeResponse> findLeagueCode(UUID leagueId) throws SQLException;

    boolean deactivateLeague(UUID leagueId) throws SQLException;
}