package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.Administrator;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.MatchResult;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class MatchResultDAOImpl extends BaseDAO implements MatchResultDAO {

    private static final Logger LOG = Logger.getLogger(MatchResultDAOImpl.class.getName());

    private static final String MATCH_RESULT_SELECT =
            "SELECT resultId, fixtureId, homeScore, awayScore, resultDate, approved, approved_by_admin_user_id AS approvedByAdminUserId, simulation_run_number AS simulationRunNumber FROM matchResult ";

    private MatchResult mapMatchResult(ResultSet result) throws SQLException {
        Fixture fixture = new Fixture();
        fixture.setFixtureId(UUID.fromString(result.getString("fixtureId")));
        MatchResult matchResult = new MatchResult();
        matchResult.setResultId(UUID.fromString(result.getString("resultId")));
        matchResult.setHomeScore(result.getInt("homeScore"));
        matchResult.setAwayScore(result.getInt("awayScore"));
        matchResult.setResultDate(result.getTimestamp("resultDate").toLocalDateTime());
        matchResult.setApproved(result.getBoolean("approved"));
        matchResult.setSimulationRunNumber(result.getInt("simulationRunNumber"));
        matchResult.setFixture(fixture);
        String approvedByAdminUserId = result.getString("approvedByAdminUserId");

        if (approvedByAdminUserId != null) {
            Administrator admin = new Administrator();
            admin.setUserId(UUID.fromString(approvedByAdminUserId));
            matchResult.setApprovedByAdmin(admin);
        }

        return matchResult;
    }

    @Override
    public Optional<MatchResult> findByFixtureId(UUID fixtureId) {
        String query = MATCH_RESULT_SELECT + "WHERE fixtureId = ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, fixtureId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMatchResult(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to retrieve match result by fixture ID.", e);
            throw new DataAccessException("Unable to retrieve match result by fixture ID.", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<MatchResult> save(MatchResult matchResult) {
        String query = "INSERT INTO matchResult (resultId, fixtureId, homeScore, awayScore, approved, approved_by_admin_user_id, simulation_run_number) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, matchResult.getResultId().toString());
            ps.setString(2, matchResult.getFixture().getFixtureId().toString());
            ps.setInt(3, matchResult.getHomeScore());
            ps.setInt(4, matchResult.getAwayScore());
            ps.setBoolean(5, matchResult.getApproved() != null && matchResult.getApproved());

            if (matchResult.getApprovedByAdmin() != null
                    && matchResult.getApprovedByAdmin().getUserId() != null) {
                ps.setString(6, matchResult.getApprovedByAdmin().getUserId().toString());
            } else {
                ps.setString(6, null);
            }

            ps.setInt(7, matchResult.getSimulationRunNumber());

            if (ps.executeUpdate() == 1) {
                return findByFixtureId(matchResult.getFixture().getFixtureId());
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save match result.", e);
            throw new DataAccessException("Unable to save match result.", e);
        }

        return Optional.empty();
    }
}
