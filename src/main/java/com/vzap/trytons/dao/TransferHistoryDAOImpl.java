package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.FantasyTeam;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.model.Transfer;
import com.vzap.trytons.model.TransferHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransferHistoryDAOImpl extends BaseDAO implements TransferHistoryDAO {

    private static final Logger LOG = Logger.getLogger(TransferHistoryDAOImpl.class.getName());

    @Override
    public Optional<TransferHistory> saveTransferHistory(TransferHistory history) {
        String query = "INSERT INTO transferHistory " +
                "(transferHistoryId, transferId, teamId, removed_player_id, added_player_id, " +
                "old_team_value, new_team_value, old_remaining_budget, new_remaining_budget, penaltyPoints, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, history.getTransferHistoryId().toString());
            ps.setString(2, history.getTransfer().getTransferId().toString());
            ps.setString(3, history.getFantasyTeam().getTeamId().toString());
            ps.setString(4, history.getRemovedPlayer() != null ? history.getRemovedPlayer().getPlayerId().toString() : null);
            ps.setString(5, history.getAddedPlayer() != null ? history.getAddedPlayer().getPlayerId().toString() : null);
            ps.setBigDecimal(6, history.getOldTeamValue());
            ps.setBigDecimal(7, history.getNewTeamValue());
            ps.setBigDecimal(8, history.getOldRemainingBudget());
            ps.setBigDecimal(9, history.getNewRemainingBudget());
            ps.setInt(10, history.getPenaltyPoints());
            ps.setTimestamp(11, Timestamp.valueOf(history.getCreatedAt()));

            if(ps.executeUpdate() == 1){
                return Optional.of(history);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to save transfer history", e);
            throw new DataAccessException("Unable to save transfer history", e);
        }
        return Optional.empty();
    }

    @Override
    public List<TransferHistory> getHistoryByTeamId(UUID teamId) {
        List <TransferHistory> historyList = new ArrayList<>();
        String query = "SELECT * FROM transferHistory WHERE teamId = ? ORDER BY createdAt DESC";

        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());

            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    historyList.add(mapHistory.rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find transfer history for team", e);
            throw new DataAccessException("Unable to find transfer history for team", e);
        }
        return historyList;
    }

    private TransferHistory mapHistory (ResultSet rs) throws SQLException {
        TransferHistory history = new TransferHistory();
        history.setTransferHistoryId(UUID.fromString(rs.getString("transferHistoryId")));
        history.setOldTeamValue(rs.getBigDecimal("old_team_value"));
        history.setNewTeamValue(rs.getBigDecimal("new_team_value"));
        history.setOldRemainingBudget(rs.getBigDecimal("old_remaining_budget"));
        history.setNewRemainingBudget(rs.getBigDecimal("new_remaining_budget"));
        history.setPenaltyPoints(rs.getInt("penaltyPoints"));
        history.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());

        Transfer transfer = new Transfer();
        transfer.setTransferId(UUID.fromString(rs.getString("transferId")));
        history.setTransfer(transfer);

        FantasyTeam team = new FantasyTeam();
        team.setTeamId(UUID.fromString(rs.getString("teamId")));
        history.setFantasyTeam(team);

        String removedId = rs.getString("removed_player_id");
        if (removedId != null){
            Player removed = new  Player();
            removed.setPlayerId(UUID.fromString(removedId));
            history.setRemovedPlayer(removed);
        }

        String addedId = rs.getString("added_player_id");
        if  (addedId != null){
            Player added = new  Player();
            added.setPlayerId(UUID.fromString(addedId));
        }
        return history;
    }
}
