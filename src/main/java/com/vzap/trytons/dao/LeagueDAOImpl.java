package com.vzap.trytons.dao;


import com.vzap.trytons.enums.LeagueType;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.League;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeagueDAOImpl extends BaseDAO implements LeagueDAO {
    //Inserted the logger.
   private static final Logger LOG = Logger.getLogger(String.valueOf(LeagueDAOImpl.class));


    // only call this after confirming the requester is the league manager
//    public Optional<LeagueCodeResponse> findLeagueCode(UUID leagueId) throws SQLException {
//        String sql = "SELECT leagueId, leagueCode FROM league WHERE leagueId = ?";
//
//        try (Connection con = getConnection();
//             PreparedStatement stmt = con.prepareStatement(sql)) {
//            stmt.setString(1, leagueId.toString());
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (!rs.next()) {
//                    return Optional.empty();
//                }
//                LeagueCodeResponse response = new LeagueCodeResponse(
//                        UUID.fromString(rs.getString("leagueId")),
//                        rs.getString("leagueCode")
//                );
//                return Optional.of(response);
//            }
//        }
//    }

    @Override
    public League saveLeague(League league) {
        return null;
    }

    @Override
    public Optional<League> findLeagueById(UUID leagueId) {
        return Optional.empty();
    }

    @Override
    public List<League> findAllLeagues() {
        return List.of();
    }

    @Override
    public List<League> findLeaguesByLeagueManager(UUID userId) {
        return List.of();
    }

    @Override
    public Optional<League> findLeagueByLeagueCode(UUID leagueCode) {
        return Optional.empty();
    }

    @Override
    public boolean existsByLeagueCode(String leagueCode) {
        return false;
    }

    @Override
    public boolean deactivateLeague(UUID leagueId) {
        String sql = "UPDATE league SET isActive = FALSE WHERE leagueId = ?";
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, leagueId.toString());
            return stmt.executeUpdate() > 0;
        }catch(SQLException e){
           LOG.log(Level.SEVERE,e.getMessage(),e);//actually get a message here
           throw new DataAccessException("League can't be deactivated", e);//for each throw one of the custom exceptions.
        }
    }

    @Override
    public League updateLeague(League league) {
        return null;
    }

    @Override
    public boolean deleteLeague(UUID leagueId) {
        return false;
    }

    private League rowToLeague(ResultSet rs) throws SQLException {
        League league = new League();
        league.setLeagueId(UUID.fromString(rs.getString("leagueId")));
        league.setLeagueName(rs.getString("leagueName"));
        league.setDescription(rs.getString("description"));
        league.setLeagueType(LeagueType.valueOf(rs.getString("leagueType")));
        league.setLeagueCode(rs.getString("leagueCode"));
        league.setCreationDate(rs.getTimestamp("creationDate").toLocalDateTime());
        league.setIsActive(rs.getBoolean("isActive"));
        league.setMaxMembers(rs.getInt("maxMembers"));
        return league;
    }


    }
