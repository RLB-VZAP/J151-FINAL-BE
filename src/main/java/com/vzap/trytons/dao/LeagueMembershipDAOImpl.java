package com.vzap.trytons.dao;

import com.vzap.trytons.enums.LeagueMemberRole;
import com.vzap.trytons.model.LeagueMembership;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeagueMembershipDAOImpl extends BaseDAO implements LeagueMembershipDAO {

    private static final Logger LOGGER = Logger.getLogger(LeagueMembershipDAOImpl.class.getName());

    private static final String BASE_FIELDS =
            "membershipId, leagueId, registered_user_id, teamId, isActive, joinDate, memberRole";

    private static final String RESPONSE_FIELDS =
            "lm.membershipId, lm.isActive, lm.joinDate, lm.memberRole,"
                    + " l.leagueId, l.leagueName,"
                    + " ru.userId AS userId, u.username,"
                    + " ft.teamId, ft.teamName";

    private static final String RESPONSE_JOIN =
            " FROM leagueMembership lm"
                    + " JOIN league l ON l.leagueId = lm.leagueId"
                    + " JOIN registeredUser ru ON ru.userId = lm.registered_user_id"
                    + " JOIN user u ON u.userId = ru.userId"
                    + " JOIN fantasyTeam ft ON ft.teamId = lm.teamId";

    @Override
    public UUID createMembership(UUID leagueId, UUID userId, UUID teamId, LeagueMemberRole role) throws SQLException {
        UUID newId = UUID.randomUUID();

        String sql = "INSERT INTO leagueMembership (membershipId, leagueId, registered_user_id, teamId, isActive, joinDate, memberRole)" + " VALUES (?, ?, ?, ?, TRUE, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, newId.toString());
            stmt.setString(2, leagueId.toString());
            stmt.setString(3, userId.toString());
            stmt.setString(4, teamId.toString());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(6, role.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create membership for league" + leagueId + " user" + userId + " team" + teamId, e);
            throw e;
        }
        return newId;
    }

    @Override
    public Optional<LeagueMembership> findById(UUID membershipId) throws SQLException {
        String sql = "SELECT " + BASE_FIELDS + " FROM leagueMembership WHERE membershipId = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, membershipId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rowToMembership(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch membership" + " " + membershipId, e);
            throw e;
        }
    }

    @Override
    public List<LeagueMembership> findActiveByLeague(UUID leagueId) throws SQLException {
        String sql = "SELECT " + BASE_FIELDS + " FROM leagueMembership WHERE leagueId = ? AND isActive = TRUE ORDER BY joinDate ASC";

        List<LeagueMembership> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToMembership(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch  active memberships for league " + leagueId, e);
            throw e;
        }
        return list;
    }

    @Override
    public List<LeagueMembership> findActiveByUser(UUID userId) throws SQLException {
        String sql = "SELECT " + BASE_FIELDS + " FROM leagueMembership WHERE registered_user_id = ? AND isActive = TRUE ORDER BY joinDate DESC";

        List<LeagueMembership> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, userId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToMembership(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch  active memberships for user " + userId, e);
            throw e;
        }
        return list;
    }

    @Override
    public List<LeagueMembershipResponse> findResponsesByLeague(UUID leagueId) throws SQLException {
        return List.of();
    }

    @Override
    public List<LeagueMembershipResponse> findResponsesByUser(UUID userId) throws SQLException {
        return List.of();
    }

    @Override
    public boolean existsActiveByLeagueAndUser(UUID leagueId, UUID userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leagueMembership WHERE leagueId = ? AND registered_user_id = ? AND isActive = TRUE";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());
            stmt.setString(2, userId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to check active membership for league " + leagueId + " user " + userId, e);
            throw e;
        }
    }

    @Override
    public int countActiveMembers(UUID leagueId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leagueMembership WHERE leagueId = ? AND isActive = TRUE";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to count active members for league " + leagueId, e);
            throw e;
        }
    }

    @Override
    public boolean deactivateMembership(UUID membershipId) throws SQLException {
        String sql = "UPDATE leagueMembership SET isActive = FALSE WHERE membershipId = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, membershipId.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to deactivate membership " + membershipId, e);
            throw e;
        }
    }


    @Override
    public boolean updateRole(UUID membershipId, LeagueMemberRole newRole) throws SQLException {
        String sql = "UPDATE leagueMembership SET memberRole = ? WHERE membershipId = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, newRole.name());
            stmt.setString(2, membershipId.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to update role for membership " + membershipId + " to  " + newRole, e);
            throw e;
        }
    }

    private LeagueMembership rowToMembership(ResultSet rs) throws SQLException {
        LeagueMembership mem = new LeagueMembership();
        mem.setMembershipId(parseUuid(rs.getString("membershipId"), "membershipId"));
        mem.setIsActive(rs.getBoolean("isActive"));
        mem.setJoinDate(parseTimestamp(rs.getTimestamp("joinDate"), "joinDate"));
        mem.setMemberRole(parseRole(rs.getString("memberRole")));
        return mem;
    }

    private UUID parseUuid(String str, String columnName) throws SQLException {
        if (str == null) {
            throw new SQLException("Unexpected null value for column " + columnName);
        }
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            throw new SQLException("incorrect UUID in column " + columnName + "  " + str, e);
        }
    }

    private LocalDateTime parseTimestamp(Timestamp ts, String columnName) throws SQLException {
        if (ts == null) {
            throw new SQLException("Unexpected null value for column " + columnName);
        }
        return ts.toLocalDateTime();
    }

    private LeagueMemberRole parseRole(String str) throws SQLException {
        if (str == null) {
            throw new SQLException("Unexpected null value for column memberRole");
        }
        try {
            return LeagueMemberRole.valueOf(str);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Unrecognized league member role: " + str, e);
        }
    }
}