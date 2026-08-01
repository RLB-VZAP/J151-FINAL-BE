package com.vzap.trytons.dao.tournament;

import com.vzap.trytons.enums.TournamentStatus;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.tournament.Tournament;

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
public class TournamentDAOImpl extends BaseDAO implements TournamentDAO {
    private static final Logger LOG = Logger.getLogger(TournamentDAOImpl.class.getName());

    @Override
    public Tournament create(Tournament tournament) {
        String query = "INSERT INTO tournament (tournamentId, leagueId, season, status, managerCount, poolCount, poolMatchdays, bracketSize, thirdPlacePlayoff) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, tournament.getTournamentId().toString());
            ps.setString(2, tournament.getLeagueId().toString());
            ps.setString(3, tournament.getSeason());
            ps.setString(4, tournament.getStatus().name());
            ps.setInt(5, tournament.getManagerCount());
            ps.setInt(6, tournament.getPoolCount());
            ps.setInt(7, tournament.getPoolMatchdays());
            ps.setInt(8, tournament.getBracketSize());
            ps.setBoolean(9, tournament.isThirdPlacePlayoff());
            if (ps.executeUpdate() == 1) {
                Optional<Tournament> created = findById(tournament.getTournamentId());
                if (created.isPresent()) {
                    return created.get();
                }
                throw new DataAccessException("Tournament was inserted, but cannot be retrieved.", null);
            }
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The tournament could not be created because it conflicts with an existing record.");
            }

            String message = e.getMessage();
            if (message != null && message.contains("uk_tournament_league_season")) {
                throw new ConflictException("A tournament already exists for this league and season.");
            }

            LOG.log(Level.SEVERE, "Unable to create tournament", e);
            throw new DataAccessException("Unable to create tournament", e);
        }
        return null;
    }

    @Override
    public Optional<Tournament> findById(UUID tournamentId) {
        String query = "SELECT * FROM tournament WHERE tournamentId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, tournamentId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.mapTournament(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament", e);
            throw new DataAccessException("Unable to find tournament", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tournament> findByLeagueAndSeason(UUID leagueId, String season) {
        String query = "SELECT * FROM tournament WHERE leagueId = ? AND season = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, leagueId.toString());
            ps.setString(2, season);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.mapTournament(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament by league and season", e);
            throw new DataAccessException("Unable to find tournament by league and season", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tournament> findActiveByLeague(UUID leagueId) {
        String query = "SELECT * FROM tournament WHERE leagueId = ? AND status IN ('POOL_STAGE','KNOCKOUT_STAGE')";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, leagueId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.mapTournament(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find active tournament by league", e);
            throw new DataAccessException("Unable to find active tournament by league", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tournament> findByLeague(UUID leagueId) {
        String query = "SELECT * FROM tournament WHERE leagueId = ?";
        List<Tournament> tournaments = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, leagueId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tournaments.add(this.mapTournament(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournaments by league", e);
            throw new DataAccessException("Unable to find tournaments by league", e);
        }
        return tournaments;
    }

    @Override
    public List<Tournament> findByStatus(TournamentStatus status) {
        String query = "SELECT * FROM tournament WHERE status = ?";
        List<Tournament> tournaments = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tournaments.add(this.mapTournament(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournaments by status", e);
            throw new DataAccessException("Unable to find tournaments by status", e);
        }
        return tournaments;
    }

    @Override
    public boolean updateStatus(UUID tournamentId, TournamentStatus status) {
        String query = "UPDATE tournament SET status = ? WHERE tournamentId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, status.name());
            ps.setString(2, tournamentId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The tournament status could not be updated because it conflicts with an existing record.");
            }
            LOG.log(Level.SEVERE, "Unable to update tournament status", e);
            throw new DataAccessException("Unable to update tournament status", e);
        }
    }

    @Override
    public boolean complete(UUID tournamentId, UUID championTeamId, UUID runnerUpTeamId, UUID thirdPlaceTeamId) {
        String query = "UPDATE tournament SET status = 'COMPLETED', champion_team_id = ?, runner_up_team_id = ?, third_place_team_id = ?, completedAt = NOW() WHERE tournamentId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, championTeamId.toString());
            ps.setString(2, runnerUpTeamId.toString());
            if (thirdPlaceTeamId != null) {
                ps.setString(3, thirdPlaceTeamId.toString());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, tournamentId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The tournament could not be completed because it conflicts with an existing record.");
            }
            LOG.log(Level.SEVERE, "Unable to complete tournament", e);
            throw new DataAccessException("Unable to complete tournament", e);
        }
    }

    @Override
    public boolean delete(UUID tournamentId) {
        String query = "DELETE FROM tournament WHERE tournamentId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, tournamentId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to delete tournament", e);
            throw new DataAccessException("Unable to delete tournament", e);
        }
    }

    private Tournament mapTournament(ResultSet rs) throws SQLException {
        Timestamp completedAtTimestamp = rs.getTimestamp("completedAt");
        Timestamp createdAtTimestamp = rs.getTimestamp("createdAt");

        String championTeamId = rs.getString("champion_team_id");
        String runnerUpTeamId = rs.getString("runner_up_team_id");
        String thirdPlaceTeamId = rs.getString("third_place_team_id");

        return Tournament.builder()
                .tournamentId(UUID.fromString(rs.getString("tournamentId")))
                .leagueId(UUID.fromString(rs.getString("leagueId")))
                .season(rs.getString("season"))
                .status(TournamentStatus.valueOf(rs.getString("status")))
                .managerCount(rs.getInt("managerCount"))
                .poolCount(rs.getInt("poolCount"))
                .poolMatchdays(rs.getInt("poolMatchdays"))
                .bracketSize(rs.getInt("bracketSize"))
                .thirdPlacePlayoff(rs.getBoolean("thirdPlacePlayoff"))
                .championTeamId(championTeamId == null ? null : UUID.fromString(championTeamId))
                .runnerUpTeamId(runnerUpTeamId == null ? null : UUID.fromString(runnerUpTeamId))
                .thirdPlaceTeamId(thirdPlaceTeamId == null ? null : UUID.fromString(thirdPlaceTeamId))
                .createdAt(createdAtTimestamp == null ? null : createdAtTimestamp.toLocalDateTime())
                .completedAt(completedAtTimestamp == null ? null : completedAtTimestamp.toLocalDateTime())
                .build();
    }
}
