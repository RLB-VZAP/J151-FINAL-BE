package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyPoints;

import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.vzap.trytons.util.DBConnectionManager.getConnection;

public class FantasyPointsDAOImpl extends BaseDAO implements FantasyPointsDAO {
    private static final Logger LOG = Logger.getLogger(FantasyPointsDAO.class.getName());
    @Override
    public FantasyPoints save(FantasyPoints points) {

        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO fantasyPoints "
                + "(pointsId, teamId, playerId, fixtureId, ruleId, pointsEarned, calculationDate, match_round_number, calculationVersion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, newId.toString());
            ps.setString(2, points.getFantasyTeam().getTeamId().toString());
            ps.setString(3, points.getPlayer().getPlayerId().toString());
            ps.setString(4, points.getFixture().getFixtureId().toString());
            if (points.getScoringRule() != null){
                ps.setString(5, points.getScoringRule().getRuleId().toString());

            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.setInt(6, points.getPointsEarned());
            ps.setTimestamp(7, Timestamp.valueOf(points.getCalculationDate()));
            ps.setInt(8, points.getMatchRoundNumber());
            ps.setInt(9, points.getCalculationVersion());

            points.setPointsId(newId);

            return points;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not save fantasy points", e);
            throw new DataAccessException("Could not save fantasy points", e);
        }
    }

    @Override
    public List<FantasyPoints> findByFixtureId(UUID fixtureId) {
        return List.of();
    }

    @Override
    public List<FantasyPoints> findByTeamId(UUID teamId) {
        return List.of();
    }
}
