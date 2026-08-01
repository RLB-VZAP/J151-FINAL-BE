package com.vzap.trytons.dao.tournament;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.tournament.TournamentStanding;

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

import com.vzap.trytons.dao.shared.BaseDAO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TournamentStandingDAOImpl extends BaseDAO implements TournamentStandingDAO {
    private static final Logger LOG = Logger.getLogger(TournamentStandingDAOImpl.class.getName());

    @Override
    public TournamentStanding create(TournamentStanding standing) {
        String query = "INSERT INTO tournament_standing (standingId, tournamentId, poolId, teamId, played, won, drawn, lost, pointsFor, pointsAgainst, attackBonus, losingBonus, tournamentPoints, position, qualified) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, standing.getStandingId().toString());
            ps.setString(2, standing.getTournamentId().toString());
            ps.setString(3, standing.getPoolId().toString());
            ps.setString(4, standing.getTeamId().toString());
            ps.setInt(5, standing.getPlayed());
            ps.setInt(6, standing.getWon());
            ps.setInt(7, standing.getDrawn());
            ps.setInt(8, standing.getLost());
            ps.setInt(9, standing.getPointsFor());
            ps.setInt(10, standing.getPointsAgainst());
            ps.setInt(11, standing.getAttackBonus());
            ps.setInt(12, standing.getLosingBonus());
            ps.setInt(13, standing.getTournamentPoints());
            if (standing.getPosition() != null) {
                ps.setInt(14, standing.getPosition());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            ps.setBoolean(15, standing.isQualified());
            if (ps.executeUpdate() == 1) {
                Optional<TournamentStanding> created = findById(standing.getStandingId());
                if (created.isPresent()) {
                    return created.get();
                }
                throw new DataAccessException("Tournament standing was inserted, but cannot be retrieved.", null);
            }
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The tournament standing could not be created because it conflicts with an existing record.");
            }

            String message = e.getMessage();
            if (message != null && message.contains("uk_tournament_standing_team")) {
                throw new ConflictException("A standing already exists for this team in this pool.");
            }

            LOG.log(Level.SEVERE, "Unable to create tournament standing", e);
            throw new DataAccessException("Unable to create tournament standing", e);
        }
        return null;
    }

    @Override
    public boolean update(TournamentStanding standing) {
        String query = "UPDATE tournament_standing SET played = ?, won = ?, drawn = ?, lost = ?, pointsFor = ?, pointsAgainst = ?, attackBonus = ?, losingBonus = ?, tournamentPoints = ?, position = ?, qualified = ? WHERE standingId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, standing.getPlayed());
            ps.setInt(2, standing.getWon());
            ps.setInt(3, standing.getDrawn());
            ps.setInt(4, standing.getLost());
            ps.setInt(5, standing.getPointsFor());
            ps.setInt(6, standing.getPointsAgainst());
            ps.setInt(7, standing.getAttackBonus());
            ps.setInt(8, standing.getLosingBonus());
            ps.setInt(9, standing.getTournamentPoints());
            if (standing.getPosition() != null) {
                ps.setInt(10, standing.getPosition());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setBoolean(11, standing.isQualified());
            ps.setString(12, standing.getStandingId().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The tournament standing could not be updated because it conflicts with an existing record.");
            }
            LOG.log(Level.SEVERE, "Unable to update tournament standing", e);
            throw new DataAccessException("Unable to update tournament standing", e);
        }
    }

    @Override
    public Optional<TournamentStanding> findByPoolAndTeam(UUID poolId, UUID teamId) {
        String query = "SELECT * FROM tournament_standing WHERE poolId = ? AND teamId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, poolId.toString());
            ps.setString(2, teamId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.mapStanding(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament standing", e);
            throw new DataAccessException("Unable to find tournament standing", e);
        }
        return Optional.empty();
    }

    @Override
    public List<TournamentStanding> findByPool(UUID poolId) {
        String query = "SELECT * FROM tournament_standing WHERE poolId = ?";
        List<TournamentStanding> standings = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, poolId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    standings.add(this.mapStanding(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament standings", e);
            throw new DataAccessException("Unable to find tournament standings", e);
        }
        return standings;
    }

    @Override
    public List<TournamentStanding> findByTournament(UUID tournamentId) {
        String query = "SELECT * FROM tournament_standing WHERE tournamentId = ?";
        List<TournamentStanding> standings = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, tournamentId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    standings.add(this.mapStanding(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament standings", e);
            throw new DataAccessException("Unable to find tournament standings", e);
        }
        return standings;
    }

    private Optional<TournamentStanding> findById(UUID standingId) {
        String query = "SELECT * FROM tournament_standing WHERE standingId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, standingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.mapStanding(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament standing", e);
            throw new DataAccessException("Unable to find tournament standing", e);
        }
        return Optional.empty();
    }

    private TournamentStanding mapStanding(ResultSet rs) throws SQLException {
        Timestamp updatedAtTimestamp = rs.getTimestamp("updatedAt");
        int position = rs.getInt("position");
        Integer positionValue = rs.wasNull() ? null : position;

        return TournamentStanding.builder()
                .standingId(UUID.fromString(rs.getString("standingId")))
                .tournamentId(UUID.fromString(rs.getString("tournamentId")))
                .poolId(UUID.fromString(rs.getString("poolId")))
                .teamId(UUID.fromString(rs.getString("teamId")))
                .played(rs.getInt("played"))
                .won(rs.getInt("won"))
                .drawn(rs.getInt("drawn"))
                .lost(rs.getInt("lost"))
                .pointsFor(rs.getInt("pointsFor"))
                .pointsAgainst(rs.getInt("pointsAgainst"))
                .pointsDifference(rs.getInt("pointsDifference"))
                .attackBonus(rs.getInt("attackBonus"))
                .losingBonus(rs.getInt("losingBonus"))
                .tournamentPoints(rs.getInt("tournamentPoints"))
                .position(positionValue)
                .qualified(rs.getBoolean("qualified"))
                .updatedAt(updatedAtTimestamp == null ? null : updatedAtTimestamp.toLocalDateTime())
                .build();
    }
}
