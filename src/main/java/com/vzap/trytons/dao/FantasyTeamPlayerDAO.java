package com.vzap.trytons.dao;

import com.vzap.trytons.model.Player;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FantasyTeamPlayerDAO extends BaseDAO {
    public void addPlayerToSquad(Player entry) throws SQLException {
        final String sql =
                "INSERT INTO players " +
                        "(team_id, player_id, is_captain, is_vice_captain, is_bench) " +
                        "VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getConnection();
            stmt = con.prepareStatement(sql);
            applyParams(stmt, entry);
            stmt.executeUpdate();

        } finally {
            closeResources(con, stmt, null);
        }
    }

    public void replaceSquad(UUID teamId, List<Player> players) throws SQLException {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("replaceSquad: squad list must not be null or empty");
        }

        final String deleteSql =
                "DELETE FROM players WHERE team_id = ?";

        final String insertSql =
                "INSERT INTO fantasy_team_players " +
                        "(team_id, player_id, is_captain, is_vice_captain, is_bench) " +
                        "VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement deleteStmt = null;
        PreparedStatement insertStmt = null;

        try {
            con = getConnection();
            con.setAutoCommit(false);
            deleteStmt = con.prepareStatement(deleteSql);
            deleteStmt.setString(1, teamId.toString());
            deleteStmt.executeUpdate();

            insertStmt = con.prepareStatement(insertSql);
            for (Player entry : players) {
                applyParams(insertStmt, entry);
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();

            con.commit();

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException rb) { rb.printStackTrace(); }
            }
            throw e;
        } finally {
            if (deleteStmt != null) {
                try { deleteStmt.close(); } catch (SQLException ignore) {}
            }
            closeResources(con, insertStmt, null);
        }
    }
    public List<Player> getSquadByTeamId(UUID teamId) throws SQLException {
        final String sql =
                "SELECT team_id, player_id, is_captain, is_vice_captain, is_bench " +
                        "FROM   fantasy_team_players " +
                        "WHERE  team_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Player> squad = new ArrayList<>();

        try {
            con  = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, teamId.toString());
            rs   = stmt.executeQuery();

            while (rs.next()) {
                squad.add(mapRow(rs));
            }
            return squad;

        } finally {
            closeResources(con, stmt, rs);
        }
    }
    public Player findSquadEntry(UUID teamId, UUID playerId) throws SQLException {
        final String sql =
                "SELECT team_id, player_id, is_captain, is_vice_captain, is_bench " +
                        "FROM   fantasy_team_players " +
                        "WHERE  team_id = ? AND player_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con  = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, teamId.toString());
            stmt.setString(2, playerId.toString());
            rs   = stmt.executeQuery();

            return rs.next() ? mapRow(rs) : null;

        } finally {
            closeResources(con, stmt, rs);
        }
    }

    public void removePlayerFromSquad(UUID teamId, UUID playerId) throws SQLException {
        final String sql =
                "DELETE FROM fantasy_team_players WHERE team_id = ? AND player_id = ?";

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con  = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, teamId.toString());
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();

        } finally {
            closeResources(con, stmt, null);
        }
    }
    private void applyParams(PreparedStatement stmt, Player entry) throws SQLException {
        stmt.setString(1, entry.getPlayerName().toString());
        stmt.setString(2, entry.getPlayerId().toString());
        stmt.setString(1,entry.getValue().toString());

    }

    private Player mapRow(ResultSet rs) throws SQLException {
        Player entry = new Player();

        entry.setPlayerName(String.valueOf(UUID.fromString(rs.getString("team_id"))));
        entry.setPlayerId(rs.getBoolean("is captain"));
        entry.setViceCaptain(rs.getBoolean("is vice captain"));
        entry.setBench(rs.getBoolean("is bench"));
        Player playerStub = new Player();
        entry.setPlayer(playerStub);

        return entry;
    }
}
