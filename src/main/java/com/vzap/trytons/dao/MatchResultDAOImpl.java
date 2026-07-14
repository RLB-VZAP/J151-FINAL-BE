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

    private static final String MATCH_RESULT_SELECT =
            "SELECT mr.resultId AS resultId, "
                    + "mr.fixtureId AS fixtureId, "
                    + "f.team_a_id AS teamAId, "
                    + "f.team_b_id AS teamBId, "
                    + "mr.team_a_score AS teamAScore, "
                    + "mr.team_b_score AS teamBScore, "
                    + "mr.winnerSide AS winnerSide, "
                    + "mr.isDraw AS isDraw, "
                    + "mr.resultDate AS resultDate, "
                    + "mr.approved AS approved, "
                    + "mr.isCurrent AS isCurrent, "
                    + "mr.approved_by_admin_user_id AS approvedByAdminId, "
                    + "mr.simulation_run_number AS simulationRunNumber "
                    + "FROM matchResult mr "
                    + "JOIN fixture f ON f.fixtureId = mr.fixtureId ";

    private MatchResult mapMatchResult(ResultSet rs) throws SQLException {
        MatchResult matchResult = new MatchResult();

        matchResult.setResultId(UUID.fromString(rs.getString("resultId")));
        matchResult.setFixtureId(UUID.fromString(rs.getString("fixtureId")));
        matchResult.setTeamAId(UUID.fromString(rs.getString("teamAId")));
        matchResult.setTeamBId(UUID.fromString(rs.getString("teamBId")));
        matchResult.setTeamAScore(rs.getInt("teamAScore"));
        matchResult.setTeamBScore(rs.getInt("teamBScore"));
        matchResult.setWinnerSide(rs.getString("winnerSide"));
        matchResult.setDraw(rs.getBoolean("isDraw"));
        matchResult.setApproved(rs.getBoolean("approved"));
        matchResult.setCurrent(rs.getBoolean("isCurrent"));
        matchResult.setSimulationRunNumber(rs.getInt("simulationRunNumber"));

        Timestamp resultDate = rs.getTimestamp("resultDate");

        matchResult.setResultDate(resultDate != null ? resultDate.toLocalDateTime() : null);

        String approvedByAdminId = rs.getString("approvedByAdminId");
        matchResult.setApprovedByAdminId(approvedByAdminId != null ? UUID.fromString(approvedByAdminId) : null);
        return matchResult;
    }

    @Override
    public Optional<MatchResult> findById(UUID resultId) {
        String query = MATCH_RESULT_SELECT + "WHERE mr.resultId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, resultId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMatchResult(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve match result by result ID.", e);
            throw new DataAccessException("Unable to retrieve match result by result ID.", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<MatchResult> findCurrentByFixtureId(UUID fixtureId) {
        String query = MATCH_RESULT_SELECT + "WHERE mr.fixtureId = ? AND mr.isCurrent = TRUE";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, fixtureId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMatchResult(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve current match result by fixture ID.", e);
            throw new DataAccessException("Unable to retrieve current match result by fixture ID.", e);
        }

        return Optional.empty();
    }

    @Override
    public List<MatchResult> findAllByFixtureId(UUID fixtureId) {
        String query = MATCH_RESULT_SELECT + "WHERE mr.fixtureId = ? ORDER BY mr.simulation_run_number ASC";

        List<MatchResult> results = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, fixtureId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapMatchResult(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve match results by fixture ID.", e);
            throw new DataAccessException("Unable to retrieve match results by fixture ID.", e);
        }

        return results;
    }

    @Override
    public MatchResult save(MatchResult matchResult) {
        UUID resultId = matchResult.getResultId() != null ? matchResult.getResultId() : UUID.randomUUID();

        String query = "INSERT INTO matchResult "
                        + "(resultId, fixtureId, team_a_score, team_b_score, "
                        + "winnerSide, isDraw, resultDate, approved, "
                        + "approved_by_admin_user_id, simulation_run_number, isCurrent) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, resultId.toString());
            ps.setString(2, matchResult.getFixtureId().toString());
            ps.setInt(3, matchResult.getTeamAScore());
            ps.setInt(4, matchResult.getTeamBScore());
            ps.setString(5, matchResult.getWinnerSide());
            ps.setBoolean(6, matchResult.isDraw());

            if (matchResult.getResultDate() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(matchResult.getResultDate()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }

            ps.setBoolean(8, matchResult.isApproved());

            if (matchResult.getApprovedByAdminId() != null) {
                ps.setString(9, matchResult.getApprovedByAdminId().toString());
            } else {
                ps.setNull(9, Types.VARCHAR);
            }
            ps.setInt(10, matchResult.getSimulationRunNumber());

            ps.setBoolean(11, matchResult.isCurrent());
            int affectedRows = ps.executeUpdate();
            if (affectedRows != 1) {throw new DataAccessException("Unable to save match result.", null);
            }
            matchResult.setResultId(resultId);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save match result.", e);
            throw new DataAccessException("Unable to save match result.", e);
        }
        return findById(resultId).orElseThrow(() -> new DataAccessException("Match result was saved but could not be retrieved.", null));
    }

    @Override
    public int getNextSimulationRunNumber(UUID fixtureId) {
        String query =
                "SELECT MAX(simulation_run_number) FROM matchResult WHERE fixtureId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, fixtureId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int currentRunNumber = rs.getInt(1);
                    if (rs.wasNull()) {
                        return 1;
                    }
                    return currentRunNumber + 1;
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to get next simulation run number.", e);
            throw new DataAccessException("Unable to get next simulation run number.", e);
        }

        throw new DataAccessException("Unable to get next simulation run number.", null);
    }

    @Override
    public int markAllFixtureResultsNotCurrent(UUID fixtureId) {
        String query =
                "UPDATE matchResult SET isCurrent = FALSE WHERE fixtureId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, fixtureId.toString());

            return ps.executeUpdate();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to mark fixture results as not current.", e);

            throw new DataAccessException("Unable to mark fixture results as not current.", e);
        }
    }
}