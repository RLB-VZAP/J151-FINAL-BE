package com.vzap.trytons.dao;

import com.vzap.trytons.model.League;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueDAO {

    Optional<League> findById(UUID leagueId) throws SQLException;

    List<League> findAllActive() throws SQLException;

    boolean existsByLeagueCode(String leagueCode) throws SQLException;

    Optional<UUID> findLeagueIdByCode(String leagueCode) throws SQLException;

    boolean deactivateLeague(UUID leagueId) throws SQLException;

    Optional<League> findByLeagueCode(String leagueCode) throws SQLException;
}