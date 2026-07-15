package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.RegisteredUser;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
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

@ApplicationScoped
public class FantasyTeamDAOImpl extends BaseDAO implements FantasyTeamDAO {

    private static final Logger LOG = Logger.getLogger(FantasyTeamDAOImpl.class.getName());

    /*
     * The current schema stores the editable team, owner and remaining budget.
     * Team value is calculated from the selected players, while fantasy points
     * are read from the latest available ranking entry.
     */
    private static final String FANTASY_TEAM_SELECT = """
            SELECT ft.teamId,
                   ft.teamName,
                   ft.remainingBudget,
                   ft.creationDate,
                   ft.isValid,
                   u.userId AS ownerUserId,
                   u.username AS ownerUsername,
                   COALESCE((
                       SELECT SUM(p.value)
                       FROM team_player_selection tps
                       JOIN player p ON p.playerId = tps.playerId
                       WHERE tps.teamId = ft.teamId
                   ), 0) AS totalTeamValue,
                   COALESCE((
                       SELECT MAX(r.total_fantasy_points)
                       FROM ranking r
                       WHERE r.teamId = ft.teamId
                   ), 0) AS totalPoints
            FROM fantasyTeam ft
            JOIN `user` u ON u.userId = ft.owner_user_id
            """;

    private FantasyTeam mapTeam(ResultSet rs) throws SQLException {
        RegisteredUser owner = new RegisteredUser();
        owner.setUserId(UUID.fromString(rs.getString("ownerUserId")));
        owner.setUsername(rs.getString("ownerUsername"));

        FantasyTeam team = new FantasyTeam();
        team.setTeamId(UUID.fromString(rs.getString("teamId")));
        team.setTeamName(rs.getString("teamName"));
        team.setTotalTeamValue(rs.getBigDecimal("totalTeamValue"));
        team.setRemainingBudget(rs.getBigDecimal("remainingBudget"));

        Timestamp creationDate = rs.getTimestamp("creationDate");
        team.setCreationDate(creationDate == null ? null : creationDate.toLocalDateTime());

        team.setTotalPoints(rs.getInt("totalPoints"));
        // TODO: FantasyTeam.weeklyPoints/isLocked removed (no column, no derivation) — model now mirrors schema.sql
        team.setWeeklyPoints(0);
        team.setIsValid(rs.getBoolean("isValid"));
        // TODO: FantasyTeam.isLocked removed and owner renamed to ownerUserId (UUID) — model now mirrors schema.sql
        team.setIsLocked(false);
        team.setOwner(owner);
        return team;
    }

    @Override
    public Optional<FantasyTeam> createTeam(FantasyTeam team) {
        UUID teamId = team.getTeamId() == null ? UUID.randomUUID() : team.getTeamId();
        LocalDateTime creationDate = team.getCreationDate() == null ? LocalDateTime.now() : team.getCreationDate();
        String sql = """
                INSERT INTO fantasyTeam
                    (teamId, owner_user_id, teamName, remainingBudget, creationDate, isValid)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, teamId.toString());
            // TODO: FantasyTeam.owner renamed to ownerUserId (UUID) — model now mirrors schema.sql
            statement.setString(2, team.getOwner().getUserId().toString());
            statement.setString(3, team.getTeamName());
            statement.setBigDecimal(4, team.getRemainingBudget() == null ? BigDecimal.ZERO : team.getRemainingBudget());
            statement.setTimestamp(5, Timestamp.valueOf(creationDate));
            statement.setBoolean(6, Boolean.TRUE.equals(team.getIsValid()));

            if (statement.executeUpdate() == 1) {
                return getTeamById(teamId);
            }
            return Optional.empty();
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message != null && message.contains("uk_fantasyTeam_owner")) {
                throw new ConflictException("This user already owns a fantasy team.");
            }
            if (message != null && message.contains("uk_fantasyTeam_teamName")) {
                throw new ConflictException("This team name is already taken.");
            }
            LOG.log(Level.SEVERE, "Unable to create fantasy team.", e);
            throw new DataAccessException("Unable to create fantasy team.", e);
        }
    }

    @Override
    public Optional<FantasyTeam> getTeamById(UUID teamId) {
        String sql = FANTASY_TEAM_SELECT + " WHERE ft.teamId = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, teamId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapTeam(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve fantasy team.", e);
            throw new DataAccessException("Unable to retrieve fantasy team.", e);
        }
    }

    @Override
    public FantasyTeam findTeamById(UUID teamId) {
        return getTeamById(teamId).orElse(null);
    }

    @Override
    public List<FantasyTeam> findTeamsByOwner(UUID ownerUserId) {
        List<FantasyTeam> teams = new ArrayList<>();
        String sql = FANTASY_TEAM_SELECT + " WHERE ft.owner_user_id = ? ORDER BY ft.creationDate DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, ownerUserId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    teams.add(mapTeam(resultSet));
                }
            }
            return teams;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve fantasy teams by owner.", e);
            throw new DataAccessException("Unable to retrieve fantasy teams by owner.", e);
        }
    }

    @Override
    public boolean updatePoints(UUID teamId, int totalPoints, int weeklyPoints) {
        String sql = "UPDATE ranking SET total_fantasy_points = ?, updatedAt = CURRENT_TIMESTAMP WHERE teamId = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, totalPoints);
            statement.setString(2, teamId.toString());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update fantasy team points.", e);
            throw new DataAccessException("Unable to update fantasy team points.", e);
        }
    }

    @Override
    public boolean updateLockedStatus(UUID teamId, boolean isLocked) {
        // Lock state belongs to fantasyRound/roundLock in the current schema,
        // not fantasyTeam. Keep this compatibility method harmless for old callers.
        return getTeamById(teamId).isPresent();
    }

    @Override
    public boolean updateBudgetAndValue(UUID teamId, BigDecimal totalTeamValue, BigDecimal remainingBudget) {
        String sql = "UPDATE fantasyTeam SET remainingBudget = ? WHERE teamId = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, remainingBudget);
            statement.setString(2, teamId.toString());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update fantasy team budget.", e);
            throw new DataAccessException("Unable to update fantasy team budget.", e);
        }
    }
}
