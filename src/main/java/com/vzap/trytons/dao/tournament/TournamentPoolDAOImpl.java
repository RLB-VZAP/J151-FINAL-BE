package com.vzap.trytons.dao.tournament;

import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.tournament.TournamentPool;
import com.vzap.trytons.model.tournament.TournamentPoolMember;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vzap.trytons.dao.shared.BaseDAO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TournamentPoolDAOImpl extends BaseDAO implements TournamentPoolDAO {
    private static final Logger LOG = Logger.getLogger(TournamentPoolDAOImpl.class.getName());

    @Override
    public TournamentPool create(TournamentPool pool) {
        String query = "INSERT INTO tournament_pool (poolId, tournamentId, poolName, poolSize) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, pool.getPoolId().toString());
            ps.setString(2, pool.getTournamentId().toString());
            ps.setString(3, pool.getPoolName());
            ps.setInt(4, pool.getPoolSize());
            if (ps.executeUpdate() == 1) {
                Optional<TournamentPool> created = findById(pool.getPoolId());
                if (created.isPresent()) {
                    return created.get();
                }
                throw new DataAccessException("Tournament pool was inserted, but cannot be retrieved.", null);
            }
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "The tournament pool could not be created because it conflicts with an existing record.");
            }

            String message = e.getMessage();
            if (message != null && message.contains("uk_tournament_pool_name")) {
                throw new ConflictException("A pool with this name already exists for this tournament.");
            }

            LOG.log(Level.SEVERE, "Unable to create tournament pool", e);
            throw new DataAccessException("Unable to create tournament pool", e);
        }
        return null;
    }

    @Override
    public Optional<TournamentPool> findById(UUID poolId) {
        String query = "SELECT * FROM tournament_pool WHERE poolId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, poolId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.mapPool(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament pool", e);
            throw new DataAccessException("Unable to find tournament pool", e);
        }
        return Optional.empty();
    }

    @Override
    public List<TournamentPool> findByTournament(UUID tournamentId) {
        String query = "SELECT * FROM tournament_pool WHERE tournamentId = ?";
        List<TournamentPool> pools = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, tournamentId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pools.add(this.mapPool(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament pools", e);
            throw new DataAccessException("Unable to find tournament pools", e);
        }
        return pools;
    }

    @Override
    public TournamentPoolMember addMember(TournamentPoolMember member) {
        String query = "INSERT INTO tournament_pool_member (poolMemberId, tournamentId, poolId, teamId, seed) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, member.getPoolMemberId().toString());
            ps.setString(2, member.getTournamentId().toString());
            ps.setString(3, member.getPoolId().toString());
            ps.setString(4, member.getTeamId().toString());
            ps.setInt(5, member.getSeed());
            if (ps.executeUpdate() == 1) {
                return findMemberById(member.getPoolMemberId())
                        .orElseThrow(() -> new DataAccessException("Tournament pool member was inserted, but cannot be retrieved.", null));
            }
        } catch (SQLException e) {
            if ("45000".equals(e.getSQLState())) {
                throw new ConflictException(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "A tournament pool member must be an active league member.");
            }

            String message = e.getMessage();
            if (message != null && message.contains("uk_tournament_member_team")) {
                throw new ConflictException("This team is already assigned to a pool in this tournament.");
            }
            if (message != null && message.contains("uk_tournament_pool_member")) {
                throw new ConflictException("This team is already a member of this pool.");
            }

            LOG.log(Level.SEVERE, "Unable to add tournament pool member", e);
            throw new DataAccessException("Unable to add tournament pool member", e);
        }
        return null;
    }

    @Override
    public List<TournamentPoolMember> findMembersByPool(UUID poolId) {
        String query = "SELECT * FROM tournament_pool_member WHERE poolId = ?";
        List<TournamentPoolMember> members = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, poolId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(this.mapMember(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament pool members", e);
            throw new DataAccessException("Unable to find tournament pool members", e);
        }
        return members;
    }

    @Override
    public List<TournamentPoolMember> findMembersByTournament(UUID tournamentId) {
        String query = "SELECT * FROM tournament_pool_member WHERE tournamentId = ?";
        List<TournamentPoolMember> members = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, tournamentId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(this.mapMember(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament pool members", e);
            throw new DataAccessException("Unable to find tournament pool members", e);
        }
        return members;
    }

    private Optional<TournamentPoolMember> findMemberById(UUID poolMemberId) {
        String query = "SELECT * FROM tournament_pool_member WHERE poolMemberId = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, poolMemberId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.mapMember(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find tournament pool member", e);
            throw new DataAccessException("Unable to find tournament pool member", e);
        }
        return Optional.empty();
    }

    private TournamentPool mapPool(ResultSet rs) throws SQLException {
        Timestamp createdAtTimestamp = rs.getTimestamp("createdAt");
        return TournamentPool.builder()
                .poolId(UUID.fromString(rs.getString("poolId")))
                .tournamentId(UUID.fromString(rs.getString("tournamentId")))
                .poolName(rs.getString("poolName"))
                .poolSize(rs.getInt("poolSize"))
                .createdAt(createdAtTimestamp == null ? null : createdAtTimestamp.toLocalDateTime())
                .build();
    }

    private TournamentPoolMember mapMember(ResultSet rs) throws SQLException {
        Timestamp createdAtTimestamp = rs.getTimestamp("createdAt");
        return TournamentPoolMember.builder()
                .poolMemberId(UUID.fromString(rs.getString("poolMemberId")))
                .tournamentId(UUID.fromString(rs.getString("tournamentId")))
                .poolId(UUID.fromString(rs.getString("poolId")))
                .teamId(UUID.fromString(rs.getString("teamId")))
                .seed(rs.getInt("seed"))
                .createdAt(createdAtTimestamp == null ? null : createdAtTimestamp.toLocalDateTime())
                .build();
    }
}
