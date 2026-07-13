package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.Administrator;
import com.vzap.trytons.model.FantasyPoints;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.MatchResult;
import jakarta.inject.Singleton;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class MatchResultDAOImpl extends BaseDAO implements MatchResultDAO {

    private static final Logger LOG = Logger.getLogger(MatchResultDAOImpl.class.getName());

    @Override
    public MatchResult save(MatchResult matchResult) {
        String query = "INSERT INTO matchResult (resultId, fixtureId, team_a_score, team_b_score, winnerSide, isDraw, resultDate, approved, approved_by_admin_user_id, simulation_run_number, isCurrent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, matchResult.getResultId().toString());
            ps.setString(2, matchResult.getFixtureId().toString());
            ps.setInt(3, matchResult.getTeamAScore());
            ps.setInt(4, matchResult.getTeamBScore());
            ps.setString(5, matchResult.getWinnerSide());
            ps.setBoolean(6, matchResult.isDraw());
            ps.setTimestamp(7, Timestamp.valueOf(matchResult.getResultDate()));
            ps.setBoolean(8, matchResult.isApproved());
            if (matchResult.getApprovedByAdminId() != null) {
                ps.setString(9, matchResult.getApprovedByAdminId().toString());
            } else {
                ps.setNull(9, Types.VARCHAR);
            }
            ps.setInt(10, matchResult.getSimulationRunNumber());
            ps.setBoolean(11, matchResult.isCurrent());

            ps.executeUpdate();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save match result.", e);
            throw new DataAccessException("Unable to save match result.", e);
        }

        return matchResult;
    }

    @Override
    public Optional<MatchResult> findById(UUID resultId) {

        String query = "SELECT resultId, matchResult.fixtureId, team_a_score, team_b_score, winnerSide, isDraw, resultDate, approved, approved_by_admin_user_id, simulation_run_number, isCurrent, fixture.team_a_id, fixture.team_b_id\n" +
                "FROM matchResult\n" +
                "INNER JOIN fixture  \n" +
                "  ON matchResult.fixtureId = fixture.fixtureId WHERE matchResult.resultId = ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)){

                ps.setString(1, resultId.toString());

                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()){
                        String approvedByAdminIdStr = rs.getString("approved_by_admin_user_id");
                        UUID approvedByAdminId;
                        if (approvedByAdminIdStr != null) {
                            approvedByAdminId = UUID.fromString(approvedByAdminIdStr);
                        } else {
                            approvedByAdminId = null;
                        }


                        MatchResult mr = MatchResult.builder()
                                .resultId(resultId)
                                .fixtureId(UUID.fromString(rs.getString("fixtureId")))
                                .teamAId(UUID.fromString(rs.getString("team_a_id")))
                                .teamBId(UUID.fromString(rs.getString("team_b_id")))
                                .winnerSide(rs.getString("winnerSide"))
                                .draw(rs.getBoolean("isDraw"))
                                .resultDate(rs.getObject("resultDate", LocalDateTime.class))
                                .approved(rs.getBoolean("approved"))
                                .approvedByAdminId(approvedByAdminId)
                                .simulationRunNumber(rs.getInt("simulation_run_number"))
                                .current(rs.getBoolean("isCurrent"))
                                .build();

                        return Optional.of(mr);

                    }
                }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find match result by ID.", e);
            throw new DataAccessException("Unable to find match result by ID.", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<MatchResult> findCurrentByFixtureId(UUID fixtureId) {
        String query = "SELECT resultId, matchResult.fixtureId, team_a_score, team_b_score, winnerSide, isDraw, resultDate, approved, approved_by_admin_user_id, simulation_run_number, isCurrent, fixture.team_a_id, fixture.team_b_id\n" +
                "FROM matchResult\n" +
                "INNER JOIN fixture  \n" +
                "  ON matchResult.fixtureId = fixture.fixtureId WHERE matchResult.fixtureId = ? AND isCurrent = TRUE";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query))   {

            ps.setString(1, fixtureId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){

                    String approvedByAdminIdStr = rs.getString("approved_by_admin_user_id");
                    UUID approvedByAdminId;
                    if (approvedByAdminIdStr != null) {
                        approvedByAdminId = UUID.fromString(approvedByAdminIdStr);
                    } else {
                        approvedByAdminId = null;
                    }


                    MatchResult mr = MatchResult.builder()
                            .resultId(UUID.fromString(rs.getString("resultId")))
                            .fixtureId(fixtureId)
                            .teamAId(UUID.fromString(rs.getString("team_a_id")))
                            .teamBId(UUID.fromString(rs.getString("team_b_id")))
                            .winnerSide(rs.getString("winnerSide"))
                            .draw(rs.getBoolean("isDraw"))
                            .resultDate(rs.getObject("resultDate", LocalDateTime.class))
                            .approved(rs.getBoolean("approved"))
                            .approvedByAdminId(approvedByAdminId)
                            .simulationRunNumber(rs.getInt("simulation_run_number"))
                            .current(rs.getBoolean("isCurrent"))
                            .build();

                    return Optional.of(mr);

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find match result by fixtureId", e);
            throw new DataAccessException("Unable to find match result by fixtureId", e);
        }

        return Optional.empty();
    }

    @Override
    public List<MatchResult> findAllByFixtureId(UUID fixtureId) {
        String query = "SELECT resultId, matchResult.fixtureId, team_a_score, team_b_score, winnerSide, isDraw, resultDate, approved, approved_by_admin_user_id, simulation_run_number, isCurrent, fixture.team_a_id, fixture.team_b_id\n" +
                "FROM matchResult\n" +
                "INNER JOIN fixture  \n" +
                "  ON matchResult.fixtureId = fixture.fixtureId WHERE matchResult.fixtureId = ? ORDER BY simulation_run_number";
        List<MatchResult> matchResults = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query))   {

            ps.setString(1, fixtureId.toString());

            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){

                    String approvedByAdminIdStr = rs.getString("approved_by_admin_user_id");
                    UUID approvedByAdminId;
                    if (approvedByAdminIdStr != null) {
                        approvedByAdminId = UUID.fromString(approvedByAdminIdStr);
                    } else {
                        approvedByAdminId = null;
                    }


                    MatchResult mr = MatchResult.builder()
                            .resultId(UUID.fromString(rs.getString("resultId")))
                            .fixtureId(fixtureId)
                            .teamAId(UUID.fromString(rs.getString("team_a_id")))
                            .teamBId(UUID.fromString(rs.getString("team_b_id")))
                            .winnerSide(rs.getString("winnerSide"))
                            .draw(rs.getBoolean("isDraw"))
                            .resultDate(rs.getObject("resultDate", LocalDateTime.class))
                            .approved(rs.getBoolean("approved"))
                            .approvedByAdminId(approvedByAdminId)
                            .simulationRunNumber(rs.getInt("simulation_run_number"))
                            .current(rs.getBoolean("isCurrent"))
                            .build();

                    matchResults.add(mr);

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find all match results by fixtureId", e);
            throw new DataAccessException("Unable to find all match results by fixtureId", e);
        }

        return matchResults;
    }


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
    public int getNextSimulationRunNumber(UUID fixtureId) {
        return 0;
    }

    @Override
    public int markAllFixtureResultsNotCurrent(UUID fixtureId) {
        return 0;
    }
}
