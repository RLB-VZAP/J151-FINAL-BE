package com.vzap.trytons.dao;

import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.model.League;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LeagueDAOImpl extends BaseDAO implements LeagueDAO {

    @Override
    public Optional<League> findById(UUID leagueId) throws SQLException {
        String sql = "SELECT leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, creationDate, isActive, maxMembers"
                + " FROM league WHERE leagueId = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rowToLeague(rs));
            }
        }
    }

    @Override
    public List<League> findAllActive() throws SQLException {
        String sql = "SELECT leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, creationDate, isActive, maxMembers"
                + " FROM league WHERE isActive = TRUE ORDER BY creationDate DESC";

        List<League> leagues = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                leagues.add(rowToLeague(rs));
            }
        }

        return leagues;
    }

    @Override
    public boolean existsByLeagueCode(String leagueCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM league WHERE leagueCode = ? AND isActive = TRUE";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueCode);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public Optional<UUID> findLeagueIdByCode(String leagueCode) throws SQLException {
        String sql = "SELECT leagueId FROM league WHERE leagueCode = ? AND isActive = TRUE";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(UUID.fromString(rs.getString("leagueId")));
            }
        }
    }


    @Override
    public boolean deactivateLeague(UUID leagueId) throws SQLException {
        String sql = "UPDATE league SET isActive = FALSE WHERE leagueId = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<League> findByLeagueCode(String leagueCode) throws SQLException {
        return Optional.empty();
    }

    private League rowToLeague(ResultSet rs) throws SQLException {
        League league = new League();
        league.setLeagueId(UUID.fromString(rs.getString("leagueId")));
        league.setLeagueName(rs.getString("leagueName"));
        league.setDescription(rs.getString("description"));
        league.setLeagueType(LeagueType.valueOf(rs.getString("leagueType")));
        league.setLeagueCode(rs.getString("leagueCode"));
        league.setCreationDate(rs.getTimestamp("creationDate").toLocalDateTime());
        league.setIsActive(rs.getBoolean("isActive"));
        league.setMaxMembers(rs.getInt("maxMembers"));
        return league;
    }
}