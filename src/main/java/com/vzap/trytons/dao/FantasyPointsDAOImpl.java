package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        String query =  "SELECT * FROM fantasyPoints WHERE pointsId = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, pointsId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){

                    FantasyPoints fp = FantasyPoints.builder()
                            .pointsId(pointsId)
                            .statId(UUID.fromString(rs.getString("statId")))
                            .totalPoints(rs.getInt("totalPoints"))
                            .calculationVersion(rs.getInt("calculationVersion"))
                            .finalVersion(rs.getBoolean("isFinal"))
                            .calculationDate(rs.getObject("calculatedAt", LocalDateTime.class))
                            .build();

                    return Optional.of(fp);

                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find fantasy points with ID", e);
            throw new DataAccessException("Unable to find fantasy points with ID", e);
        }
        return Optional.empty();

    }

    @Override
    public List<FantasyPoints> findByStatId(UUID statId) {

        String query = "SELECT * FROM fantasyPoints WHERE statId = ?";
        List<FantasyPoints> fantasyPoints = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query))   {

            ps.setString(1, statId.toString());

            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){

                    FantasyPoints fp = FantasyPoints.builder()
                            .pointsId(UUID.fromString(rs.getString("pointsId")))
                            .statId(statId)
                            .totalPoints(rs.getInt("totalPoints"))
                            .calculationVersion(rs.getInt("calculationVersion"))
                            .finalVersion(rs.getBoolean("isFinal"))
                            .calculationDate(rs.getObject("calculatedAt", LocalDateTime.class))
                            .build();

                    fantasyPoints.add(fp);

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find fantasy points by statId", e);
            throw new DataAccessException("Unable to find fantasy points by statId", e);
        }

        return fantasyPoints;
    }

    @Override
    public Optional<FantasyPoints> findFinalByStatId(UUID statId) {

        String query = "SELECT * FROM fantasyPoints WHERE isFinal = ? AND statId = ?";

        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            ps.setBoolean(1, true);
            ps.setString(2, statId.toString());

            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    FantasyPoints fp = FantasyPoints.builder()
                            .pointsId(UUID.fromString(rs.getString("pointsId")))
                            .statId(statId)
                            .totalPoints(rs.getInt("totalPoints"))
                            .calculationVersion(rs.getInt("calculationVersion"))
                            .finalVersion(rs.getBoolean("isFinal"))
                            .calculationDate(rs.getObject("calculatedAt", LocalDateTime.class))
                            .build();

                    return Optional.of(fp);

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find fantasy points by statId", e);
            throw new DataAccessException("Unable to find fantasy points by statId", e);
        }

        return Optional.empty();
    }

    @Override
    public int markExistingPointsForStatAsNotFinal(UUID statId) {

        String query = "UPDATE fantasyPoints SET isFinal = ? WHERE statId = ? AND isFinal = ?";

        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){

            ps.setBoolean(1, false);
            ps.setString(2, statId.toString());
            ps.setBoolean(3, true);

                return ps.executeUpdate();

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to mark existing points for stat as not final", e);
            throw new DataAccessException("Unable to mark existing points for stat as not final", e);
        }

    }

    @Override
    public int getNextCalculationVersion(UUID statId) {

        String query = "SELECT calculationVersion FROM fantasyPoints WHERE statId = ? ORDER BY calculationVersion DESC LIMIT 1";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){

            ps.setString(1, statId.toString());

            try(ResultSet rs = ps.executeQuery()){

                if (rs.next()){

                    return rs.getInt("calculationVersion") + 1;

                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to get the next calculation version", e);
            throw new DataAccessException("Unable to get the next calculation version", e);
        }

        return 1;
    }


}
