package com.vzap.trytons.dao;

import com.vzap.trytons.enums.TransferStatus;
import com.vzap.trytons.enums.TransferWindowStatus;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.*;
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
                "(transferId, teamId, roundId, removed_player_id, added_player_id, transferDate, " +
                "removed_player_value, added_player_value, penaltyPoints, status, confirmedAt, created_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, transfer.getTransferId().toString());
            ps.setString(2, transfer.getFantasyTeam().getTeamId().toString());
            ps.setString(3, transfer.getRound().getRoundId().toString());
            ps.setString(4, transfer.getRemovedPlayer().getPlayerId().toString());
            ps.setString(5, transfer.getAddedPlayer().getPlayerId().toString());
            ps.setTimestamp(6, Timestamp.valueOf(transfer.getTransferDate()));
            ps.setBigDecimal(7, transfer.getRemoved_player_value());
            ps.setBigDecimal(8, transfer.getAdded_player_value());
            ps.setInt(9, transfer.getPenaltyPoints());
            ps.setString(10, transfer.getStatus().name());

            if (transfer.getConfirmationDate() != null) {
                ps.setTimestamp(11, Timestamp.valueOf(transfer.getConfirmationDate()));
            } else {
                ps.setNull(11, Types.TIMESTAMP);
            }
            ps.setString(12, transfer.getCreatedBy().getUserId().toString());

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
        String query = "SELECT * FROM transfer WHERE transferId = ?";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){

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
        String query = "UPDATE transfer SET status = ?, confirmedAt = ? WHERE transferId = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){

            ps.setString(1, transferStatus.name());

            if(confirmationDate != null){
                ps.setTimestamp(2, Timestamp.valueOf(confirmationDate));
            }else{
                ps.setNull(2, Types.TIMESTAMP);
            }

            ps.setString(3, transferId.toString());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update transfer status", e);
            throw new DataAccessException("Unable to update transfer status", e);
        }
    }

    @Override
    public int countConfirmedTransfers(UUID teamId, UUID roundId) {
        String query = "SELECT COUNT(*) AS confirmedCount FROM transfer " +
                "WHERE teamId = ? AND roundId = ? AND status = 'CONFIRMED'";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());
            ps.setString(2, roundId.toString());

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt("confirmedCount");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to count confirmed transfers", e);
            throw new DataAccessException("Unable to count confirmed transfers", e);
        }
        return 0;
    }

    @Override
    public List<Transfer> getTransfersByRound(UUID roundId) {
        String query = "SELECT * FROM transfer WHERE roundId = ? ORDER BY transferDate DESC";
        List<Transfer> transfers = new ArrayList<>();

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, roundId.toString());

            try (ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    transfers.add(mapTransfer(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfers from the round", e);
            throw new DataAccessException("Unable to find transfers from the round", e);
        }
        return transfers;
    }

    private Transfer mapTransfer(ResultSet rs) throws SQLException {
        Transfer transfer = new Transfer();

        transfer.setTransferId(UUID.fromString(rs.getString("transferId")));
        transfer.setTransferDate(rs.getTimestamp("transferDate").toLocalDateTime());
        transfer.setRemoved_player_value(rs.getBigDecimal("removed_player_value"));
        transfer.setAdded_player_value(rs.getBigDecimal("added_player_value"));
        transfer.setValueDifference(rs.getBigDecimal("valueDifference"));
        transfer.setPenaltyPoints(rs.getInt("penaltyPoints"));
        transfer.setStatus(TransferStatus.valueOf(rs.getString("status")));

        Timestamp confirmedAt = rs.getTimestamp("confirmedAt");
        transfer.setConfirmationDate(confirmedAt != null ? confirmedAt.toLocalDateTime() : null);

        FantasyTeam team = new FantasyTeam();
        team.setTeamId(UUID.fromString(rs.getString("teamId")));
        transfer.setFantasyTeam(team);

        FantasyRound round = new FantasyRound();
        round.setRoundId(UUID.fromString(rs.getString("roundId")));
        transfer.setRound(round);

        Player removed = new Player();
        removed.setPlayerId(UUID.fromString(rs.getString("removed_player_id")));
        transfer.setRemovedPlayer(removed);

        Player added = new Player();
        added.setPlayerId(UUID.fromString(rs.getString("added_player_id")));
        transfer.setAddedPlayer(added);

        RegisteredUser createdBy = new RegisteredUser();
        createdBy.setUserId(UUID.fromString(rs.getString("created_by_user_id")));
        transfer.setCreatedBy(createdBy);

        return transfer;
    }
}
