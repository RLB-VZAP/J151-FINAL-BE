package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyPoints;
import jakarta.enterprise.context.ApplicationScoped;

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
public class FantasyPointsDAOImpl extends BaseDAO implements FantasyPointsDAO {

    private static final Logger LOG = Logger.getLogger(FantasyPointsDAOImpl.class.getName());

    private FantasyPoints mapRow(ResultSet rs) throws SQLException {
        Timestamp calculatedAt = rs.getTimestamp("calculatedAt");
        return FantasyPoints.builder()
                .pointsId(UUID.fromString(rs.getString("pointsId")))
                .statId(UUID.fromString(rs.getString("statId")))
                .totalPoints(rs.getInt("totalPoints"))
                .calculationVersion(rs.getInt("calculationVersion"))
                .finalVersion(rs.getBoolean("isFinal"))
                .calculationDate(calculatedAt == null ? null : calculatedAt.toLocalDateTime())
                .build();
    }

    @Override
    public FantasyPoints save(FantasyPoints points) {
        UUID pointsId = points.getPointsId() == null ? UUID.randomUUID() : points.getPointsId();
        LocalDateTime calculatedAt = points.getCalculationDate() == null
                ? LocalDateTime.now()
                : points.getCalculationDate();

        String sql = """
                INSERT INTO fantasyPoints
                    (pointsId, statId, totalPoints, calculationVersion, calculatedAt, isFinal)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pointsId.toString());
            statement.setString(2, points.getStatId().toString());
            statement.setInt(3, points.getTotalPoints());
            statement.setInt(4, points.getCalculationVersion());
            statement.setTimestamp(5, Timestamp.valueOf(calculatedAt));
            statement.setBoolean(6, points.isFinalVersion());
            statement.executeUpdate();

            points.setPointsId(pointsId);
            points.setCalculationDate(calculatedAt);
            return points;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save fantasy points.", e);
            throw new DataAccessException("Unable to save fantasy points.", e);
        }
    }

    @Override
    public Optional<FantasyPoints> findById(UUID pointsId) {
        String sql = "SELECT * FROM fantasyPoints WHERE pointsId = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pointsId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve fantasy points.", e);
            throw new DataAccessException("Unable to retrieve fantasy points.", e);
        }
    }

    @Override
    public List<FantasyPoints> findByStatId(UUID statId) {
        List<FantasyPoints> results = new ArrayList<>();
        String sql = "SELECT * FROM fantasyPoints WHERE statId = ? ORDER BY calculationVersion";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, statId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
            }
            return results;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve fantasy points by statistic.", e);
            throw new DataAccessException("Unable to retrieve fantasy points by statistic.", e);
        }
    }

    @Override
    public Optional<FantasyPoints> findFinalByStatId(UUID statId) {
        String sql = "SELECT * FROM fantasyPoints WHERE statId = ? AND isFinal = TRUE LIMIT 1";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, statId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve final fantasy points.", e);
            throw new DataAccessException("Unable to retrieve final fantasy points.", e);
        }
    }

    @Override
    public int markExistingPointsForStatAsNotFinal(UUID statId) {
        String sql = "UPDATE fantasyPoints SET isFinal = FALSE WHERE statId = ? AND isFinal = TRUE";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, statId.toString());
            return statement.executeUpdate();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to mark fantasy points as non-final.", e);
            throw new DataAccessException("Unable to mark fantasy points as non-final.", e);
        }
    }

    @Override
    public int getNextCalculationVersion(UUID statId) {
        String sql = "SELECT COALESCE(MAX(calculationVersion), 0) + 1 AS nextVersion FROM fantasyPoints WHERE statId = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, statId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("nextVersion") : 1;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to determine the next fantasy-points version.", e);
            throw new DataAccessException("Unable to determine the next fantasy-points version.", e);
        }
    }
}
