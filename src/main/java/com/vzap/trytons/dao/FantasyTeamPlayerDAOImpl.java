package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.TeamPlayerSelection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FantasyTeamPlayerDAOImpl extends BaseDAO implements FantasyTeamPlayerDAO {

    private static final Logger LOG = Logger.getLogger(FantasyTeamPlayerDAOImpl.class.getName());
    private static final String SELECTION_SELECT = "SELECT"
            + "selectionId, teamId, playerId, selectedDate, isCaptain, is_vice_captain, isActive "
            + "FROM fantasy_team_player";
    //made a constant when selecting from database to reduce typing. Efficiency :)

    private TeamPlayerSelection mapSelection(ResultSet rs) throws SQLException {
        FantasyTeam team = new FantasyTeam();
        team.setTeamId(readUuid(rs, "teamId"));

        Player player = new Player();
        player.setPlayerId(readUuid(rs, "playerId"));

        TeamPlayerSelection selection = new TeamPlayerSelection();
        selection.setSelectionId(readUuid(rs, "selectionId"));

        Timestamp selectedDate = rs.getTimestamp("selectedDate");
        selection.setSelectedDate(selectedDate != null ? selectedDate.toLocalDateTime() : null);

        selection.setIsCaptain(rs.getBoolean("isCaptain"));
        selection.setIsViceCaptain(rs.getBoolean("isViceCaptain"));
        selection.setFantasyTeam(team);
        selection.setPlayer(player);

        return selection;
    }

    private UUID readUuid(ResultSet rs, String name) throws SQLException {
        String value =  rs.getString(name);

        if (value == null){
            throw new SQLException("Database column " + name + " contains null value");
        }

        try{
            return UUID.fromString(value);
        }catch (IllegalArgumentException e){
            throw new SQLException("Database column " + name + " contains invalid UUID");
        }
    }

    @Override
    public boolean addPlayerToSquad(UUID teamId, UUID playerId) {
        String query = "INSERT INTO team_player_selection "
                + "(selectionId, teamId, playerId, selectedDate, isCaptain, is_vice_captain, isActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, teamId.toString());
            ps.setString(3, playerId.toString());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(5, false);
            ps.setBoolean(6, false);
            ps.setBoolean(7, true);

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            String message = e.getMessage();

            if(message != null && message.contains("uk_team_player_selection_active"))
                throw new ConflictException("Player is already in the squad");

            LOG.log(Level.SEVERE, "Could not add player to squad", e);
            throw new DataAccessException("Could not add player to squad", e);
        }
    }

    @Override
    public void replaceSquad(UUID teamId, List<UUID> playerIds) {
        String deleteQuery = "DELETE FROM team_player_selection WHERE teamId = ?";
        String insertQuery = "INSERT INTO team_player_selection "
                + "(selectionId, teamId, playerId, selectedDate, isCaptain, is_vice_captain, isActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = null; //Declared at the top instead of in try resource for rollback

        try {
            con = getConnection();
            con.setAutoCommit(false);

            try (
                    PreparedStatement dps = con.prepareStatement(deleteQuery);
                    PreparedStatement ips = con.prepareStatement(insertQuery)
            ) {
                dps.setString(1, teamId.toString());
                dps.executeUpdate();

                LocalDateTime now = LocalDateTime.now();

                for (UUID playerId : playerIds) {
                    ips.setString(1, UUID.randomUUID().toString());
                    ips.setString(2, teamId.toString());
                    ips.setString(3, playerId.toString());
                    ips.setTimestamp(4, Timestamp.valueOf(now));
                    ips.setBoolean(5, false);
                    ips.setBoolean(6, false);
                    ips.setBoolean(7, true);
                    ips.addBatch();
                }

                if (!playerIds.isEmpty()) {
                    ips.executeBatch();
                }

                con.commit();
            }

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }

            LOG.log(Level.SEVERE, "Could not replace squad", e);
            throw new DataAccessException("Could not replace squad", e);

        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    LOG.log(Level.SEVERE, "Could not close database connection", e);
                }
            }
        }
    }

    @Override
    public List<TeamPlayerSelection> getSquadByTeamId(UUID teamId) {
        String query = SELECTION_SELECT + " WHERE teamId = ? AND isActive = TRUE ORDER BY selectedDate ASC";
        List<TeamPlayerSelection> squad = new ArrayList<>();

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, teamId.toString());

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    squad.add(mapSelection(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not get team by its ID", e);
            throw new DataAccessException("Could not get team by its ID", e);
        }
        return squad;
    }

    @Override
    public Optional<TeamPlayerSelection> findSquadEntry(UUID teamId, UUID playerId) {
        String query = SELECTION_SELECT + " WHERE teamId = ? AND playerId = ? AND isActive = TRUE";

        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            ps.setString(2, playerId.toString());

            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSelection(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not get squad entry", e);
            throw new DataAccessException("Could not get squad entry", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean removePlayerFromSquad(UUID teamId, UUID playerId) {
        String query =  "DELETE FROM team_player_selection WHERE teamId = ? AND playerId = ? AND isActive = TRUE";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            ps.setString(2, playerId.toString());

            return ps.executeUpdate() >= 1;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not remove player from squad", e);
            throw new DataAccessException("Could not remove player from squad", e);
        }
    }
}
