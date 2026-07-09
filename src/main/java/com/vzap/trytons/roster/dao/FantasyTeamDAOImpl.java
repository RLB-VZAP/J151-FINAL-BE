package com.vzap.trytons.roster.dao;
import com.vzap.trytons.shared.dao.BaseDAO;

import com.vzap.trytons.shared.exceptions.ConflictException;
import com.vzap.trytons.shared.exceptions.DataAccessException;
import com.vzap.trytons.roster.model.FantasyTeam;
import com.vzap.trytons.auth.model.RegisteredUser;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FantasyTeamDAOImpl extends BaseDAO implements FantasyTeamDAO{

    private static final Logger LOG = Logger.getLogger(FantasyTeamDAOImpl.class.getName());
    private static final String FANTASY_TEAM_SELECT = "SELECT "
                    + "ft.teamId AS teamId, "
                    + "ft.teamName AS teamName, "
                    + "ft.total_team_value AS totalTeamValue, "
                    + "ft.remainingBudget AS remainingBudget, "
                    + "ft.creationDate AS creationDate, "
                    + "ft.totalPoints AS totalPoints, "
                    + "ft.weeklyPoints AS weeklyPoints, "
                    + "ft.isValid AS isValid, "
                    + "ft.isLocked AS isLocked, "
                    + "u.userId AS ownerUserId, "
                    + "u.username AS ownerUsername "
                    + "FROM fantasyTeam ft "
                    + "JOIN user u ON ft.owner_user_id = u.userId ";

    private FantasyTeam mapTeam (ResultSet rs) throws SQLException {
        RegisteredUser owner = new RegisteredUser();
        owner.setUserId(readUuid(rs, "ownerUserId"));
        owner.setUsername(rs.getString("ownerUsername"));

        FantasyTeam team = new FantasyTeam();
        team.setTeamId(readUuid(rs, "teamId"));
        team.setTeamName(rs.getString("teamName"));
        team.setTotalTeamValue(rs.getBigDecimal("totalTeamValue"));
        team.setRemainingBudget(rs.getBigDecimal("remainingBudget"));

        Timestamp creationDate = rs.getTimestamp("creationDate");
        team.setCreationDate(creationDate != null ? creationDate.toLocalDateTime() : null);

        team.setTotalPoints(rs.getInt("totalPoints"));
        team.setWeeklyPoints(rs.getInt("weeklyPoints"));
        team.setIsValid(rs.getBoolean("isValid"));
        team.setIsLocked(rs.getBoolean("isLocked"));
        team.setOwner(owner);

        return team;
    }

    private UUID readUuid(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);

        if (value == null){
            throw new SQLException("Database coloumn " + columnName + " not found");
        }

        try{
            return UUID.fromString(value);
        }catch (IllegalArgumentException e){
            throw new SQLException("Invalid UUID in coloumn " + columnName + ": " + value, e);
        }

    }
    @Override
    public Optional<FantasyTeam> createTeam(FantasyTeam team) {
        UUID  teamId = team.getTeamId() != null ? team.getTeamId() : UUID.randomUUID();
        LocalDateTime creationDate =  team.getCreationDate() != null ? team.getCreationDate() : LocalDateTime.now();

        //This is acting funny, so just keep an eye on it
        String query = "INSERT INTO fantasyTeam ("
                + "teamId, owner_user_id, teamName, total_team_value, remainingBudget, "
                + "creationDate, totalPoints, weeklyPoints, isValid, isLocked"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, teamId.toString());
            ps.setString(2, team.getOwner().getUserId().toString());
            ps.setString(3, team.getTeamName());
            ps.setBigDecimal(4, team.getTotalTeamValue() != null?  team.getTotalTeamValue() : BigDecimal.ZERO);
            ps.setBigDecimal(5, team.getRemainingBudget() != null?  team.getRemainingBudget() : BigDecimal.ZERO);
            ps.setTimestamp(6, Timestamp.valueOf(creationDate));
            ps.setInt(7, team.getTotalPoints());
            ps.setInt(8, team.getWeeklyPoints());
            ps.setBoolean(9, team.getIsValid());
            ps.setBoolean(10, team.getIsLocked());

            if(ps.executeUpdate() == 1){
                return getTeamById(teamId);
            }
        } catch (SQLException e) {
            String message = e.getMessage();

            if(message != null && message.contains("uk_fantasyTeam_owner"))
                throw new ConflictException("This user already owns a fantasy team");

            if(message != null && message.contains("uk_fantasyTeam_teamId"))
                throw new ConflictException("This team name is already taken");

            LOG.log(Level.SEVERE, "Unable to create a fantasy team", e);
            throw new DataAccessException("Unable to create fantasy team", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<FantasyTeam> getTeamById(UUID teamId) {
        String query = FANTASY_TEAM_SELECT + " WHERE ft.teamId = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, teamId.toString());

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapTeam(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find fantasy team with ID", e);
            throw new DataAccessException("Unable to find fantasy team with ID", e);
        }
        return Optional.empty();
    }

    //This naming convention was used in the code - instead of redoing the code I simply mapped
    //one find method to the other, to reduce redundant code, but made both naming conventions
    //applicable and usable.
    @Override
    public FantasyTeam findTeamById(UUID teamId) {
        return getTeamById(teamId).orElse(null);
    }

    @Override
    public List<FantasyTeam> findTeamsByOwner(UUID ownerId) {
        String query = FANTASY_TEAM_SELECT + " WHERE ft.owner_user_id = ? ORDER BY fr.creationDate ASC";
        List<FantasyTeam> teams = new ArrayList<>();

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, ownerId.toString());

            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    teams.add(mapTeam(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to find fantasy team by owner", e);
            throw new DataAccessException("Unable to find fantasy team by owner", e);
        }
        return teams;
    }

    @Override
    public boolean updatePoints(UUID teamId, int totalPoints, int weeklyPoints) {
        String query = "UPDATE fantasyTeam SET totalPoints = ?, weeklyPoints = ? WHERE teamId = ?";

        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, totalPoints);
            ps.setInt(2, weeklyPoints);
            ps.setString(3, teamId.toString());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update fantasy team points", e);
            throw new DataAccessException("Unable to update fantasy team points", e);
        }
    }

    @Override
    public boolean updateLockedStatus(UUID teamId, boolean isLocked) {
        String query = "UPDATE fantasyTeam SET isLocked = ? WHERE teamId = ?";

        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setBoolean(1, isLocked);
            ps.setString(2, teamId.toString());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update fantasy team lock status", e);
            throw new DataAccessException("Unable to update fantasy team lock status", e);
        }
    }

    @Override
    public boolean updateBudgetAndValue(UUID teamId, BigDecimal totalTeamValue, BigDecimal remainingBudget) {
        String query =  "UPDATE fantasyTeam SET remainingBudget = ?, total_team_value = ? WHERE teamId = ?";

        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {
            ps.setBigDecimal(1, remainingBudget);
            ps.setBigDecimal(2, totalTeamValue);
            ps.setString(3, teamId.toString());

            return  ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to update fantasy team budget and value", e);
            throw new DataAccessException("Unable to update fantasy team budget and value", e);
        }
    }
}
