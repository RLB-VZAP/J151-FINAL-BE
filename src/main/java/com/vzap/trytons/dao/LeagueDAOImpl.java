package com.vzap.trytons.dao;

import com.vzap.trytons.dto.CreateLeagueRequest;
import com.vzap.trytons.dto.LeagueCodeResponse;
import com.vzap.trytons.dto.LeagueResponse;
import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.model.League;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LeagueDAOImpl extends BaseDAO implements LeagueDAO {

    @Override
    public UUID createLeague(CreateLeagueRequest request, UUID managerId, String leagueCode) throws SQLException {
        UUID newId = UUID.randomUUID();

        String sql = "INSERT INTO league (leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, creationDate, isActive, maxMembers)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?)";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, newId.toString());
            stmt.setString(2, managerId.toString());
            stmt.setString(3, request.getLeagueName());
            stmt.setString(4, request.getDescription());
            stmt.setString(5, request.getLeagueType().name());
            stmt.setString(6, leagueCode);
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(8, request.getMaxMembers());
            stmt.executeUpdate();
        }

        return newId;
    }

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
    public Optional<LeagueResponse> findResponseById(UUID leagueId) throws SQLException {
        // joins manager username and counts active members in one query
        String sql = "SELECT l.leagueId, l.leagueName, l.description, l.leagueType, l.creationDate, l.isActive, l.maxMembers,"
                + " l.manager_user_id, u.username AS managerUsername, COUNT(lm.membershipId) AS currentMemberCount"
                + " FROM league l"
                + " LEFT JOIN registeredUser ru ON ru.userId = l.manager_user_id"
                + " LEFT JOIN user u ON u.userId = ru.userId"
                + " LEFT JOIN leagueMembership lm ON lm.leagueId = l.leagueId AND lm.isActive = TRUE"
                + " WHERE l.leagueId = ?"
                + " GROUP BY l.leagueId, u.username";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rowToLeagueResponse(rs));
            }
        }
    }

    @Override
    public List<LeagueResponse> findResponsesByManager(UUID managerId) throws SQLException {
        String sql = "SELECT l.leagueId, l.leagueName, l.description, l.leagueType, l.creationDate, l.isActive, l.maxMembers,"
                + " l.manager_user_id, u.username AS managerUsername, COUNT(lm.membershipId) AS currentMemberCount"
                + " FROM league l"
                + " LEFT JOIN registeredUser ru ON ru.userId = l.manager_user_id"
                + " LEFT JOIN user u ON u.userId = ru.userId"
                + " LEFT JOIN leagueMembership lm ON lm.leagueId = l.leagueId AND lm.isActive = TRUE"
                + " WHERE l.manager_user_id = ?"
                + " GROUP BY l.leagueId, u.username"
                + " ORDER BY l.creationDate DESC";

        List<LeagueResponse> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, managerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToLeagueResponse(rs));
                }
            }
        }

        return list;
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
    // only call this after confirming the requester is the league manager
    public Optional<LeagueCodeResponse> findLeagueCode(UUID leagueId) throws SQLException {
        String sql = "SELECT leagueId, leagueCode FROM league WHERE leagueId = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                LeagueCodeResponse response = new LeagueCodeResponse(
                        UUID.fromString(rs.getString("leagueId")),
                        rs.getString("leagueCode")
                );
                return Optional.of(response);
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

    private LeagueResponse rowToLeagueResponse(ResultSet rs) throws SQLException {
        LeagueResponse dto = new LeagueResponse();
        dto.setLeagueId(UUID.fromString(rs.getString("leagueId")));
        dto.setLeagueName(rs.getString("leagueName"));
        dto.setDescription(rs.getString("description"));
        dto.setLeagueType(LeagueType.valueOf(rs.getString("leagueType")));
        dto.setCreationDate(rs.getTimestamp("creationDate").toLocalDateTime());
        dto.setIsActive(rs.getBoolean("isActive"));
        dto.setMaxMembers(rs.getInt("maxMembers"));
        dto.setCurrentMemberCount(rs.getInt("currentMemberCount"));

        String rawId = rs.getString("manager_user_id");
        if (rawId != null) {
            dto.setManagerId(UUID.fromString(rawId));
            dto.setManagerUsername(rs.getString("managerUsername"));
        }

        return dto;
    }
}