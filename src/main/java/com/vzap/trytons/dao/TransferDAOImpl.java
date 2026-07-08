package com.vzap.trytons.dao;

import com.vzap.trytons.enums.TransferWindowStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.Transfer;
import jakarta.inject.Singleton;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class TransferDAOImpl extends BaseDAO implements TransferDAO {

    private static final Logger LOG = Logger.getLogger(TransferDAOImpl.class.getName());

    @Override
    public Optional<Transfer> saveTransfer(Transfer transfer) {
        String query = "INSERT INTO `transfer` " +
                "(transferId, teamId, removed_player_id, added_player_id, transferDate, " +
                "penaltyApplied, penaltyPoints, transfer_window_status, roundNumber, confirmed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, transfer.getTransferId().toString());
            ps.setString(2, transfer.getFantasyTeam().getTeamId().toString());
            ps.setString(3, transfer.getRemovedPlayer() != null ? transfer.getRemovedPlayer().getPlayerId().toString() : null);
            ps.setString(4, transfer.getAddedPlayer() != null ? transfer.getAddedPlayer().getPlayerId().toString() : null);
            ps.setTimestamp(5, Timestamp.valueOf(transfer.getTransferDate()));
            ps.setBoolean(6, Boolean.TRUE.equals(transfer.getPenaltyApplied()));
            ps.setInt(7, transfer.getPenaltyPoints());
            ps.setString(8, transfer.getTransferWindowStatus().toString());

            if (transfer.getRoundNumber() > 0) {
                ps.setInt(9, transfer.getRoundNumber());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.setBoolean(10, Boolean.TRUE.equals(transfer.getConfirmed()));

            if (ps.executeUpdate() == 1) {
                return Optional.of(transfer);
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save transfer", e);
            throw new DataAccessException("Unable to save transfer", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Transfer> getTransferById(UUID transferId) {
        String query = "SELECT * FROM `transfer` WHERE transferId = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, transferId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTransfer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfer by ID", e);
            throw new DataAccessException("Unable to find transfer by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Transfer> getTransfersByTeamId(UUID teamId) {
        String query = "SELECT * FROM `transfer` WHERE teamId = ? ORDER BY transferDate DESC";
        List<Transfer> transfers = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transfers.add(mapTransfer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfers for team", e);
            throw new DataAccessException("Unable to find transfers for team", e);
        }

        return transfers;
    }

    @Override
    public List<Transfer> findHistoryForTeam(UUID teamId) {
        String query = "SELECT * FROM `transfer` WHERE teamId = ? ORDER BY transferDate DESC";
        List<Transfer> transfers = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transfers.add(mapTransfer(rs));
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfer history for team", e);
            throw new DataAccessException("Unable to find transfer history for team", e);
        }

        return transfers;
    }

    @Override
    public int countTransfersForTeamInRound(UUID teamId, int roundNumber) {
        String query = "SELECT COUNT(*) FROM `transfer` " +
                "WHERE teamId = ? AND roundNumber = ? AND confirmed = TRUE";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());
            ps.setInt(2, roundNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to count transfers for team in round", e);
            throw new DataAccessException("Unable to count transfers for team in round", e);
        }

        return 0;
    }

    @Override
    public boolean existsDuplicateTransfer(
            UUID teamId,
            UUID removedPlayerId,
            UUID addedPlayerId,
            int roundNumber
    ) {
        String query = "SELECT COUNT(*) FROM `transfer` " +
                "WHERE teamId = ? " +
                "AND roundNumber = ? " +
                "AND confirmed = TRUE " +
                "AND ((? IS NULL AND removed_player_id IS NULL) OR removed_player_id = ?) " +
                "AND ((? IS NULL AND added_player_id IS NULL) OR added_player_id = ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());
            ps.setInt(2, roundNumber);

            String removedId = removedPlayerId != null ? removedPlayerId.toString() : null;
            String addedId = addedPlayerId != null ? addedPlayerId.toString() : null;

            ps.setString(3, removedId);
            ps.setString(4, removedId);
            ps.setString(5, addedId);
            ps.setString(6, addedId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to check duplicate transfer", e);
            throw new DataAccessException("Unable to check duplicate transfer", e);
        }
    }

    @Override
    public boolean existsPlayerConflict(UUID teamId, UUID playerId, int roundNumber) {
        String query = "SELECT COUNT(*) FROM `transfer` " +
                "WHERE teamId = ? " +
                "AND roundNumber = ? " +
                "AND confirmed = TRUE " +
                "AND (removed_player_id = ? OR added_player_id = ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());
            ps.setInt(2, roundNumber);
            ps.setString(3, playerId.toString());
            ps.setString(4, playerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to check player transfer conflict", e);
            throw new DataAccessException("Unable to check player transfer conflict", e);
        }
    }

    private Transfer mapTransfer(ResultSet rs) throws SQLException {
        Transfer transfer = new Transfer();

        transfer.setTransferId(UUID.fromString(rs.getString("transferId")));
        transfer.setTransferDate(rs.getTimestamp("transferDate").toLocalDateTime());
        transfer.setPenaltyApplied(rs.getBoolean("penaltyApplied"));
        transfer.setPenaltyPoints(rs.getInt("penaltyPoints"));
        transfer.setTransferWindowStatus(TransferWindowStatus.valueOf(rs.getString("transfer_window_status")));
        transfer.setRoundNumber(rs.getInt("roundNumber"));
        transfer.setConfirmed(rs.getBoolean("confirmed"));

        FantasyTeam team = new FantasyTeam();
        team.setTeamId(UUID.fromString(rs.getString("teamId")));
        transfer.setFantasyTeam(team);

        String removedId = rs.getString("removed_player_id");
        if (removedId != null) {
            Player removed = new Player();
            removed.setPlayerId(UUID.fromString(removedId));
            transfer.setRemovedPlayer(removed);
        }

        String addedId = rs.getString("added_player_id");
        if (addedId != null) {
            Player added = new Player();
            added.setPlayerId(UUID.fromString(addedId));
            transfer.setAddedPlayer(added);
        }

        return transfer;
    }
}