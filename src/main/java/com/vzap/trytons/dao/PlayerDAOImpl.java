package com.vzap.trytons.dao;

import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.Club;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.PlayerAvailability;
import com.vzap.trytons.model.Position;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PlayerDAOImpl extends BaseDAO implements PlayerDAO {

    private static final Logger LOG =
            Logger.getLogger(PlayerDAOImpl.class.getName());

    private static final String PLAYER_SELECT =
            "SELECT "
                    + "p.playerId AS playerId, "
                    + "p.playerName AS playerName, "
                    + "p.value AS playerValue, "
                    + "p.attackingAbility AS attackingAbility, "
                    + "p.defensiveAbility AS defensiveAbility, "
                    + "p.kickingAbility AS kickingAbility, "
                    + "p.discipline AS discipline, "
                    + "p.consistency AS consistency, "
                    + "p.fitness AS fitness, "
                    + "p.currentForm AS currentForm, "
                    + "p.total_fantasy_points AS totalFantasyPoints, "
                    + "p.isActive AS playerIsActive, "
                    + "c.clubId AS clubId, "
                    + "c.clubName AS clubName, "
                    + "c.location AS clubLocation, "
                    + "c.homeVenue AS homeVenue, "
                    + "c.strengthRating AS strengthRating, "
                    + "c.isActive AS clubIsActive, "
                    + "pos.positionId AS positionId, "
                    + "pos.positionName AS positionName, "
                    + "pos.positionCategory AS positionCategory, "
                    + "pos.minRequired AS minRequired, "
                    + "pos.maxAllowed AS maxAllowed "
                    + "FROM player p "
                    + "JOIN club c ON p.clubId = c.clubId "
                    + "JOIN position pos ON p.positionId = pos.positionId ";

    private Player mapPlayer(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setClubId(readUuid(rs, "clubId"));
        club.setClubName(rs.getString("clubName"));
        club.setLocation(rs.getString("clubLocation"));
        club.setHomeVenue(rs.getString("homeVenue"));
        club.setStrengthRating(rs.getInt("strengthRating"));
        club.setActive(rs.getBoolean("clubIsActive"));

        Position position = new Position();
        position.setPositionId(readUuid(rs, "positionId"));
        position.setPositionName(rs.getString("positionName"));
        position.setPositionCategory(rs.getString("positionCategory"));
        position.setMinRequired(rs.getInt("minRequired"));
        position.setMaxAllowed(rs.getInt("maxAllowed"));

        Player player = new Player();
        player.setPlayerId(readUuid(rs, "playerId"));
        player.setPlayerName(rs.getString("playerName"));
        player.setValue(rs.getBigDecimal("playerValue"));
        player.setAttackingAbility(rs.getInt("attackingAbility"));
        player.setDefensiveAbility(rs.getInt("defensiveAbility"));
        player.setKickingAbility(rs.getInt("kickingAbility"));
        player.setDiscipline(rs.getInt("discipline"));
        player.setConsistency(rs.getInt("consistency"));
        player.setFitness(rs.getInt("fitness"));
        player.setCurrentForm(rs.getInt("currentForm"));
        player.setTotalFantasyPoints(rs.getInt("totalFantasyPoints"));
        player.setActive(rs.getBoolean("playerIsActive"));
        player.setClub(club);
        player.setPosition(position);

        return player;
    }

    private PlayerAvailability mapAvailability(ResultSet rs) throws SQLException {
        String statusValue = rs.getString("status");

        if (statusValue == null) {
            throw new SQLException("Availability status cannot be null.");
        }

        Player player = new Player();
        player.setPlayerId(readUuid(rs, "playerId"));

        PlayerAvailability availability = new PlayerAvailability();
        availability.setAvailabilityId(readUuid(rs, "availabilityId"));

        try {
            availability.setStatus(AvailabilityStatus.valueOf(statusValue));
        } catch (IllegalArgumentException e) {
            throw new SQLException("Invalid availability status stored in database: " + statusValue, e);
        }

        Date effectiveDate = rs.getDate("effectiveDate");
        Date endDate = rs.getDate("endDate");

        availability.setEffectiveDate(effectiveDate != null ? effectiveDate.toLocalDate() : null);

        availability.setEndDate(endDate != null ? endDate.toLocalDate() : null);
        availability.setNotes(rs.getString("notes"));
        availability.setPlayer(player);

        return availability;
    }

    private UUID readUuid(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);

        if (value == null) {
            throw new SQLException("Database column '" + columnName + "' contains a null UUID.");
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Invalid UUID in database column '" + columnName + "': " + value, e);
        }
    }

    private List<Player> executePlayerList(String query, List<Object> parameters, String errorMessage) {
        List<Player> players = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            bindParameters(ps, parameters);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(mapPlayer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, errorMessage, e);
            throw new DataAccessException(errorMessage, e);
        }
        return players;
    }

    private void bindParameters(PreparedStatement ps, List<Object> parameters) throws SQLException {

        for (int index = 0; index < parameters.size(); index++) {
            Object parameter = parameters.get(index);
            int jdbcIndex = index + 1;

            if (parameter instanceof UUID uuid) {
                ps.setString(jdbcIndex, uuid.toString());
            } else if (parameter instanceof BigDecimal decimal) {
                ps.setBigDecimal(jdbcIndex, decimal);
            } else if (parameter instanceof Integer integer) {
                ps.setInt(jdbcIndex, integer);
            } else if (parameter instanceof Boolean bool) {
                ps.setBoolean(jdbcIndex, bool);
            } else if (parameter instanceof AvailabilityStatus status) {
                ps.setString(jdbcIndex, status.name());
            } else {
                ps.setString(jdbcIndex, String.valueOf(parameter));
            }
        }
    }

    @Override
    public Optional<Player> getPlayerById(UUID playerId) {
        String query = PLAYER_SELECT + "WHERE p.playerId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPlayer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve player by ID.", e);
            throw new DataAccessException("Unable to retrieve player by ID.", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Player> getAllPlayers() {
        String query = PLAYER_SELECT + "ORDER BY p.playerName ASC";

        return executePlayerList(query, List.of(), "Unable to retrieve all players.");
    }

    @Override
    public List<Player> searchPlayers(
            String playerName,
            UUID clubId,
            UUID positionId,
            BigDecimal minValue,
            BigDecimal maxValue,
            Integer minTotalFantasyPoints,
            Integer maxTotalFantasyPoints,
            Integer minCurrentForm,
            Integer maxCurrentForm,
            AvailabilityStatus availabilityStatus,
            Boolean isActive) {
        StringBuilder query = new StringBuilder(PLAYER_SELECT);
        List<Object> parameters = new ArrayList<>();

        query.append("WHERE 1 = 1 ");

        if (playerName != null && !playerName.isBlank()) {
            query.append("AND LOWER(p.playerName) LIKE ? ");
            parameters.add("%" + playerName.trim().toLowerCase(Locale.ROOT) + "%");
        }

        if (clubId != null) {
            query.append("AND p.clubId = ? ");
            parameters.add(clubId);
        }

        if (positionId != null) {
            query.append("AND p.positionId = ? ");
            parameters.add(positionId);
        }

        if (minValue != null) {
            query.append("AND p.value >= ? ");
            parameters.add(minValue);
        }

        if (maxValue != null) {
            query.append("AND p.value <= ? ");
            parameters.add(maxValue);
        }

        if (minTotalFantasyPoints != null) {
            query.append("AND p.total_fantasy_points >= ? ");
            parameters.add(minTotalFantasyPoints);
        }

        if (maxTotalFantasyPoints != null) {
            query.append("AND p.total_fantasy_points <= ? ");
            parameters.add(maxTotalFantasyPoints);
        }

        if (minCurrentForm != null) {
            query.append("AND p.currentForm >= ? ");
            parameters.add(minCurrentForm);
        }

        if (maxCurrentForm != null) {
            query.append("AND p.currentForm <= ? ");
            parameters.add(maxCurrentForm);
        }

        if (availabilityStatus != null) {
            query.append(
                    "AND EXISTS ( "
                            + "SELECT 1 "
                            + "FROM playerAvailability pa "
                            + "WHERE pa.playerId = p.playerId "
                            + "AND pa.status = ? "
                            + "AND pa.effectiveDate <= CURRENT_DATE "
                            + "AND (pa.endDate IS NULL OR pa.endDate >= CURRENT_DATE) "
                            + ") ");
            parameters.add(availabilityStatus);
        }

        if (isActive != null) {
            query.append("AND p.isActive = ? ");
            parameters.add(isActive);
        }

        query.append("ORDER BY p.playerName ASC");
        return executePlayerList(query.toString(), parameters, "Unable to search players.");
    }

    @Override
    public Optional<Player> createPlayer(Player player) {
        UUID playerId = player.getPlayerId() != null ? player.getPlayerId(): UUID.randomUUID();

        String query =
                "INSERT INTO player ("
                        + "playerId, clubId, positionId, playerName, value, "
                        + "attackingAbility, defensiveAbility, kickingAbility, "
                        + "discipline, consistency, fitness, currentForm"
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());
            ps.setString(2, player.getClub().getClubId().toString());
            ps.setString(3, player.getPosition().getPositionId().toString());
            ps.setString(4, player.getPlayerName());
            ps.setBigDecimal(5, player.getValue());
            ps.setInt(6, player.getAttackingAbility());
            ps.setInt(7, player.getDefensiveAbility());
            ps.setInt(8, player.getKickingAbility());
            ps.setInt(9, player.getDiscipline());
            ps.setInt(10, player.getConsistency());
            ps.setInt(11, player.getFitness());
            ps.setInt(12, player.getCurrentForm());

            if (ps.executeUpdate() == 1) {
                return getPlayerById(playerId);
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to create player.", e);
            throw new DataAccessException("Unable to create player.", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Player> updatePlayer(Player player) {
        String query =
                "UPDATE player SET "
                        + "clubId = ?, "
                        + "positionId = ?, "
                        + "playerName = ?, "
                        + "value = ?, "
                        + "attackingAbility = ?, "
                        + "defensiveAbility = ?, "
                        + "kickingAbility = ?, "
                        + "discipline = ?, "
                        + "consistency = ?, "
                        + "fitness = ?, "
                        + "currentForm = ? "
                        + "WHERE playerId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, player.getClub().getClubId().toString());
            ps.setString(2, player.getPosition().getPositionId().toString());
            ps.setString(3, player.getPlayerName());
            ps.setBigDecimal(4, player.getValue());
            ps.setInt(5, player.getAttackingAbility());
            ps.setInt(6, player.getDefensiveAbility());
            ps.setInt(7, player.getKickingAbility());
            ps.setInt(8, player.getDiscipline());
            ps.setInt(9, player.getConsistency());
            ps.setInt(10, player.getFitness());
            ps.setInt(11, player.getCurrentForm());
            ps.setString(12, player.getPlayerId().toString());

            if (ps.executeUpdate() == 1) {
                return getPlayerById(player.getPlayerId());
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update player.", e);
            throw new DataAccessException("Unable to update player.", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean deactivatePlayer(UUID playerId) {
        String query =
                "UPDATE player "
                        + "SET isActive = FALSE "
                        + "WHERE playerId = ? AND isActive = TRUE";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to deactivate player.", e);
            throw new DataAccessException("Unable to deactivate player.", e);
        }
    }

    @Override
    public List<Player> getPlayersByClubId(UUID clubId) {
        String query = PLAYER_SELECT + "WHERE p.clubId = ? " + "ORDER BY p.playerName ASC";

        return executePlayerList(query, List.of(clubId), "Unable to retrieve players for the club.");
    }

    @Override
    public List<Player> getPlayersByPositionId(UUID positionId) {
        String query = PLAYER_SELECT + "WHERE p.positionId = ? " + "ORDER BY p.playerName ASC";

        return executePlayerList(query, List.of(positionId), "Unable to retrieve players for the position.");
    }

    @Override
    public Optional<PlayerAvailability> getCurrentAvailability(UUID playerId) {
        String query =
                "SELECT "
                        + "pa.availabilityId, "
                        + "pa.playerId, "
                        + "pa.status, "
                        + "pa.effectiveDate, "
                        + "pa.endDate, "
                        + "pa.notes "
                        + "FROM playerAvailability pa "
                        + "WHERE pa.playerId = ? "
                        + "AND pa.effectiveDate <= CURRENT_DATE "
                        + "AND (pa.endDate IS NULL OR pa.endDate >= CURRENT_DATE) "
                        + "ORDER BY pa.effectiveDate DESC, pa.availabilityId DESC "
                        + "LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAvailability(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve current player availability.", e);
            throw new DataAccessException("Unable to retrieve current player availability.", e);
        }
        return Optional.empty();
    }

    @Override
    public List<PlayerAvailability> getAvailabilityHistory(UUID playerId) {
        String query =
                "SELECT "
                        + "pa.availabilityId, "
                        + "pa.playerId, "
                        + "pa.status, "
                        + "pa.effectiveDate, "
                        + "pa.endDate, "
                        + "pa.notes "
                        + "FROM playerAvailability pa "
                        + "WHERE pa.playerId = ? "
                        + "ORDER BY pa.effectiveDate DESC, pa.availabilityId DESC";

        List<PlayerAvailability> availabilityHistory = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, playerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    availabilityHistory.add(mapAvailability(rs));
                }
            }

        } catch (SQLException e) {LOG.log(Level.SEVERE, "Unable to retrieve player availability history.", e);throw new DataAccessException("Unable to retrieve player availability history.", e);
        }
        return availabilityHistory;
    }
}