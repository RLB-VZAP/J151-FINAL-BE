package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.MatchResult;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class MatchResultDAOImpl extends BaseDAO implements MatchResultDAO {

    private static final Logger LOG =
            Logger.getLogger(MatchResultDAOImpl.class.getName());

    private static final String MATCH_RESULT_SELECT = """
            SELECT
                mr.resultId AS resultId,
                mr.fixtureId AS fixtureId,
                f.team_a_id AS teamAId,
                f.team_b_id AS teamBId,
                mr.team_a_score AS teamAScore,
                mr.team_b_score AS teamBScore,
                mr.winnerSide AS winnerSide,
                mr.isDraw AS isDraw,
                mr.resultDate AS resultDate,
                mr.approved AS approved,
                mr.approved_by_admin_user_id AS approvedByAdminId,
                mr.simulation_run_number AS simulationRunNumber,
                mr.isCurrent AS isCurrent
            FROM matchResult mr
            JOIN fixture f
                ON f.fixtureId = mr.fixtureId
            """;

    private MatchResult mapMatchResult(ResultSet resultSet)
            throws SQLException {

        Timestamp resultDate =
                resultSet.getTimestamp("resultDate");

        String approvedByAdminId =
                resultSet.getString("approvedByAdminId");

        return MatchResult.builder()
                .resultId(
                        UUID.fromString(
                                resultSet.getString("resultId")
                        )
                )
                .fixtureId(
                        UUID.fromString(
                                resultSet.getString("fixtureId")
                        )
                )
                .teamAId(
                        UUID.fromString(
                                resultSet.getString("teamAId")
                        )
                )
                .teamBId(
                        UUID.fromString(
                                resultSet.getString("teamBId")
                        )
                )
                .teamAScore(
                        resultSet.getInt("teamAScore")
                )
                .teamBScore(
                        resultSet.getInt("teamBScore")
                )
                .winnerSide(
                        resultSet.getString("winnerSide")
                )
                .draw(
                        resultSet.getBoolean("isDraw")
                )
                .resultDate(
                        resultDate == null
                                ? null
                                : resultDate.toLocalDateTime()
                )
                .approved(
                        resultSet.getBoolean("approved")
                )
                .approvedByAdminId(
                        approvedByAdminId == null
                                ? null
                                : UUID.fromString(approvedByAdminId)
                )
                .simulationRunNumber(
                        resultSet.getInt("simulationRunNumber")
                )
                .current(
                        resultSet.getBoolean("isCurrent")
                )
                .build();
    }

    @Override
    public MatchResult save(MatchResult matchResult) {

        if (matchResult == null) {
            throw new DataAccessException(
                    "Match result cannot be null.",
                    null
            );
        }

        if (matchResult.getFixtureId() == null) {
            throw new DataAccessException(
                    "Fixture ID is required when saving a match result.",
                    null
            );
        }

        UUID resultId = matchResult.getResultId() == null
                ? UUID.randomUUID()
                : matchResult.getResultId();

        String query = """
                INSERT INTO matchResult
                    (
                        resultId,
                        fixtureId,
                        team_a_score,
                        team_b_score,
                        winnerSide,
                        isDraw,
                        resultDate,
                        approved,
                        approved_by_admin_user_id,
                        simulation_run_number,
                        isCurrent
                    )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setString(
                    1,
                    resultId.toString()
            );

            statement.setString(
                    2,
                    matchResult.getFixtureId().toString()
            );

            statement.setInt(
                    3,
                    matchResult.getTeamAScore()
            );

            statement.setInt(
                    4,
                    matchResult.getTeamBScore()
            );

            statement.setString(
                    5,
                    matchResult.getWinnerSide()
            );

            statement.setBoolean(
                    6,
                    matchResult.isDraw()
            );

            if (matchResult.getResultDate() == null) {
                statement.setNull(
                        7,
                        Types.TIMESTAMP
                );
            } else {
                statement.setTimestamp(
                        7,
                        Timestamp.valueOf(
                                matchResult.getResultDate()
                        )
                );
            }

            statement.setBoolean(
                    8,
                    matchResult.isApproved()
            );

            if (matchResult.getApprovedByAdminId() == null) {
                statement.setNull(
                        9,
                        Types.VARCHAR
                );
            } else {
                statement.setString(
                        9,
                        matchResult
                                .getApprovedByAdminId()
                                .toString()
                );
            }

            statement.setInt(
                    10,
                    matchResult.getSimulationRunNumber()
            );

            statement.setBoolean(
                    11,
                    matchResult.isCurrent()
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new DataAccessException(
                        "Unable to save match result.",
                        null
                );
            }

            matchResult.setResultId(resultId);

            return findById(resultId)
                    .orElseThrow(() ->
                            new DataAccessException(
                                    "The match result was saved "
                                            + "but could not be retrieved.",
                                    null
                            )
                    );

        } catch (SQLException e) {
            LOG.log(
                    Level.SEVERE,
                    "Unable to save match result.",
                    e
            );

            throw new DataAccessException(
                    "Unable to save match result.",
                    e
            );
        }
    }

    @Override
    public Optional<MatchResult> findById(UUID resultId) {

        if (resultId == null) {
            return Optional.empty();
        }

        String query =
                MATCH_RESULT_SELECT
                        + " WHERE mr.resultId = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setString(
                    1,
                    resultId.toString()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapMatchResult(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            LOG.log(
                    Level.SEVERE,
                    "Unable to retrieve match result by ID.",
                    e
            );

            throw new DataAccessException(
                    "Unable to retrieve match result by ID.",
                    e
            );
        }

        return Optional.empty();
    }

    @Override
    public Optional<MatchResult> findCurrentByFixtureId(
            UUID fixtureId) {

        if (fixtureId == null) {
            return Optional.empty();
        }

        String query =
                MATCH_RESULT_SELECT
                        + """
                           WHERE mr.fixtureId = ?
                             AND mr.isCurrent = TRUE
                           ORDER BY mr.simulation_run_number DESC
                           LIMIT 1
                           """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setString(
                    1,
                    fixtureId.toString()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapMatchResult(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            LOG.log(
                    Level.SEVERE,
                    "Unable to retrieve the current match result.",
                    e
            );

            throw new DataAccessException(
                    "Unable to retrieve the current match result.",
                    e
            );
        }

        return Optional.empty();
    }

    @Override
    public List<MatchResult> findAllByFixtureId(
            UUID fixtureId) {

        List<MatchResult> results = new ArrayList<>();

        if (fixtureId == null) {
            return results;
        }

        String query =
                MATCH_RESULT_SELECT
                        + """
                           WHERE mr.fixtureId = ?
                           ORDER BY mr.simulation_run_number ASC
                           """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setString(
                    1,
                    fixtureId.toString()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    results.add(
                            mapMatchResult(resultSet)
                    );
                }
            }

            return results;

        } catch (SQLException e) {
            LOG.log(
                    Level.SEVERE,
                    "Unable to retrieve match results "
                            + "for the fixture.",
                    e
            );

            throw new DataAccessException(
                    "Unable to retrieve match results "
                            + "for the fixture.",
                    e
            );
        }
    }

    @Override
    public int getNextSimulationRunNumber(
            UUID fixtureId) {

        if (fixtureId == null) {
            throw new DataAccessException(
                    "Fixture ID is required.",
                    null
            );
        }

        String query = """
                SELECT MAX(simulation_run_number)
                FROM matchResult
                WHERE fixtureId = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setString(
                    1,
                    fixtureId.toString()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    int currentRunNumber =
                            resultSet.getInt(1);

                    if (resultSet.wasNull()) {
                        return 1;
                    }

                    return currentRunNumber + 1;
                }
            }

            return 1;

        } catch (SQLException e) {
            LOG.log(
                    Level.SEVERE,
                    "Unable to determine the next "
                            + "simulation run number.",
                    e
            );

            throw new DataAccessException(
                    "Unable to determine the next "
                            + "simulation run number.",
                    e
            );
        }
    }

    @Override
    public int markAllFixtureResultsNotCurrent(
            UUID fixtureId) {

        if (fixtureId == null) {
            throw new DataAccessException(
                    "Fixture ID is required.",
                    null
            );
        }

        String query = """
                UPDATE matchResult
                SET isCurrent = FALSE
                WHERE fixtureId = ?
                  AND isCurrent = TRUE
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setString(
                    1,
                    fixtureId.toString()
            );

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOG.log(
                    Level.SEVERE,
                    "Unable to mark previous fixture "
                            + "results as not current.",
                    e
            );

            throw new DataAccessException(
                    "Unable to mark previous fixture "
                            + "results as not current.",
                    e
            );
        }
    }
}