package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.*;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FantasyPointsDAOImpl extends BaseDAO implements FantasyPointsDAO {
    private static final Logger LOG = Logger.getLogger(FantasyPointsDAOImpl.class.getName());

    @Override
    public FantasyPoints save(FantasyPoints points) {

        UUID newId = UUID.randomUUID();
        String query = "INSERT INTO fantasyPoints "
                + "(pointsId, statId, totalPoints, calculationDate, calculationVersion, finalVersion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, newId.toString());
            ps.setString(2, points.getStatId().toString());
            ps.setInt(3, points.getTotalPoints());
            ps.setTimestamp(4, Timestamp.valueOf(points.getCalculationDate()));
            ps.setInt(5, points.getCalculationVersion());
            ps.setBoolean(6, points.isFinalVersion());

            ps.executeUpdate();

            points.setPointsId(newId);

            return points;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not save fantasy points", e);
            throw new DataAccessException("Could not save fantasy points", e);
        }
    }

    @Override
    public Optional<FantasyPoints> findById(UUID pointsId) {
        throw new UnsupportedOperationException("FantasyPointsDAOImpl stub: findById is not implemented yet.");
    }

    @Override
    public List<FantasyPoints> findByStatId(UUID statId) {
        throw new UnsupportedOperationException("FantasyPointsDAOImpl stub: findByStatId is not implemented yet.");
    }

    @Override
    public Optional<FantasyPoints> findFinalByStatId(UUID statId) {
        throw new UnsupportedOperationException("FantasyPointsDAOImpl stub: findFinalByStatId is not implemented yet.");
    }

    @Override
    public int markExistingPointsForStatAsNotFinal(UUID statId) {
        throw new UnsupportedOperationException("FantasyPointsDAOImpl stub: markExistingPointsForStatAsNotFinal is not implemented yet.");
    }

    @Override
    public int getNextCalculationVersion(UUID statId) {
        throw new UnsupportedOperationException("FantasyPointsDAOImpl stub: getNextCalculationVersion is not implemented yet.");
    }


}
