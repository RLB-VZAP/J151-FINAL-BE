package com.vzap.trytons.dao;

import com.vzap.trytons.enums.TransferStatus;
import com.vzap.trytons.enums.TransferWindowStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.Transfer;
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
public class TransferDAOImpl extends BaseDAO implements TransferDAO {

    private static final Logger LOG =  Logger.getLogger(TransferDAOImpl.class.getName());

    @Override
    public Optional<Transfer> saveTransfer(Transfer transfer) {
        String query = "INSERT INTO transfer " +
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

            if(transfer.getRoundNumber() > 0){
                ps.setInt(9, transfer.getRoundNumber());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.setBoolean(10, Boolean.TRUE.equals(transfer.getConfirmed()));

            if(ps.executeUpdate() == 1){
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
        String query = "SELECT * FROM transfer WHERE transferId = ?";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query);){
            ps.setString(1, transferId.toString());

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapTransfer(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfers from the ID", e);
            throw new DataAccessException("Unable to find transfers from the ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Transfer> getTransfersByTeamId(UUID teamId) {
        String query = "SELECT * FROM transfer WHERE teamId = ? ORDER BY transferDate DESC";
        List<Transfer> transfers = new ArrayList<>();

        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, teamId.toString());

            try (ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    transfers.add(mapTransfer(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfers for the team", e);
            throw new DataAccessException("Unable to find transfers for the team", e);
        }
        return transfers;
    }

    @Override
    public boolean updateTransferStatus(UUID transferId, TransferStatus transferStatus, LocalDateTime confirmationDate) {
        return false;
    }

    @Override
    public int countConfirmedTransfers(UUID teamId, UUID roundId) {
        return 0;
    }

    @Override
    public List<Transfer> getTransfersByRound(UUID roundId) {
        return List.of();
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

        String removeId = rs.getString("removed_player_id");
        if (removeId != null){
            Player removed = new Player();
            removed.setPlayerId(UUID.fromString(removeId));
            transfer.setRemovedPlayer(removed);
        }

        String addedId =  rs.getString("added_player_id");
        if (addedId != null){
            Player added = new Player();
            added.setPlayerId(UUID.fromString(addedId));
            transfer.setAddedPlayer(added);
        }
        return transfer;
    }
}
