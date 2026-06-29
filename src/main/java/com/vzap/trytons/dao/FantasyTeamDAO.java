package com.vzap.trytons.dao;

import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.RegisteredUser;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class FantasyTeamDAO extends BaseDAO {
    public void createTeam(FantasyTeam team) throws SQLException {
        final String sql =
                "INSERT INTO fantasy_teams " +
                        "(team_id, team_name, total_value, rem_budget, creation_date, " +
                        " total_points, weekly_points, is_valid, is_locked, owner_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getConnection();
            con.setAutoCommit(false);

            stmt = con.prepareStatement(sql);
            stmt.setString(1,  team.getTeamId().toString());
            stmt.setString(2,  team.getTeamName());
            stmt.setBigDecimal(3, team.getTotalTeamValue() != null
                    ? team.getTotalTeamValue() : BigDecimal.ZERO);
            stmt.setBigDecimal(4, team.getRemainingBudget());
            stmt.setTimestamp(5, team.getCreationDate() != null
                    ? Timestamp.valueOf(team.getCreationDate()) : new Timestamp(System.currentTimeMillis()));
            stmt.setInt(6,     team.getTotalPoints());
            stmt.setInt(7,     team.getWeeklyPoints());
            stmt.setBoolean(8, Boolean.TRUE.equals(team.getIsValid()));
            stmt.setBoolean(9, Boolean.TRUE.equals(team.getIsLocked()));
            stmt.setString(10, team.getOwner().getUserId().toString());

            stmt.executeUpdate();
            con.commit();

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException rb) { rb.printStackTrace(); }
            }
            throw e;
        } finally {
            closeResources(con, stmt, null);
        }
    }

    public FantasyTeam findTeamById(UUID teamId) throws SQLException {
        final String sql =
                "SELECT team_id, team_name, total_value, rem_budget, creation_date, " +
                        "       total_points, weekly_points, is_valid, is_locked, owner_id " +
                        "FROM   fantasy_teams " +
                        "WHERE  team_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con  = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, teamId.toString());
            rs   = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } finally {
            closeResources(con, stmt, rs);
        }
    }


    public List<FantasyTeam> findTeamsByOwner(UUID ownerId) throws SQLException {
        final String sql =
                "SELECT team_id, team_name, total_value, rem_budget, creation_date, " +
                        "       total_points, weekly_points, is_valid, is_locked, owner_id " +
                        "FROM   fantasy_teams " +
                        "WHERE  owner_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<FantasyTeam> teams = new ArrayList<>();

        try {
            con  = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, ownerId.toString());
            rs   = stmt.executeQuery();

            while (rs.next()) {
                teams.add(mapRow(rs));
            }
            return teams;

        } finally {
            closeResources(con, stmt, rs);
        }
    }
    public void updatePoints(UUID teamId, int totalPoints, int weeklyPoints) throws SQLException {
        final String sql =
                "UPDATE fantasy_teams " +
                        "SET    total_points = ?, weekly_points = ? " +
                        "WHERE  team_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getConnection();
            con.setAutoCommit(false);

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, totalPoints);
            stmt.setInt(2, weeklyPoints);
            stmt.setString(3, teamId.toString());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                con.rollback();
                throw new SQLException("updatePoints: no team found with id " + teamId);
            }
            con.commit();

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException rb) { rb.printStackTrace(); }
            }
            throw e;
        } finally {
            closeResources(con, stmt, null);
        }
    }

    public void updateBudget(UUID teamId, BigDecimal totalTeamValue, BigDecimal remainingBudget)
            throws SQLException {
        final String sql =
                "UPDATE fantasy_teams " +
                        "SET    total_value = ?, rem_budget = ? " +
                        "WHERE  team_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getConnection();
            con.setAutoCommit(false);

            stmt = con.prepareStatement(sql);
            stmt.setBigDecimal(1, totalTeamValue);
            stmt.setBigDecimal(2, remainingBudget);
            stmt.setString(3, teamId.toString());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                con.rollback();
                throw new SQLException("updateBudget: no team found with id " + teamId);
            }
            con.commit();

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException rb) { rb.printStackTrace(); }
            }
            throw e;
        } finally {
            closeResources(con, stmt, null);
        }
    }
    public void updateLockedStatus(UUID teamId, boolean isLocked) throws SQLException {
        final String sql =
                "UPDATE fantasy_teams SET is_locked = ? WHERE team_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setBoolean(1, isLocked);
            stmt.setString(2, teamId.toString());
            stmt.executeUpdate();

        } finally {
            closeResources(con, stmt, null);
        }
    }

    private FantasyTeam mapRow(ResultSet rs) throws SQLException {
        FantasyTeam team = new FantasyTeam();

        team.setTeamId(UUID.fromString(rs.getString("team_id")));
        team.setTeamName(rs.getString("team_name"));
        team.setTotalTeamValue(rs.getBigDecimal("total_value"));
        team.setRemainingBudget(rs.getBigDecimal("rem_budget"));

        Timestamp ts = rs.getTimestamp("creation_date");
        if (ts != null) {
            team.setCreationDate(ts.toLocalDateTime());
        }

        team.setTotalPoints(rs.getInt("total_points"));
        team.setWeeklyPoints(rs.getInt("weekly_points"));
        team.setIsValid(rs.getBoolean("is_valid"));
        team.setIsLocked(rs.getBoolean("is_locked"));

        RegisteredUser ownerStub = new RegisteredUser();
        ownerStub.setUserId(UUID.fromString(rs.getString("owner_id")));
        team.setOwner(ownerStub);

        return team;
    }
}