package com.vzap.trytons.dao;

import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.model.Club;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClubDAOImpl extends BaseDAO implements ClubDAO {
    private static final Logger LOG = Logger.getLogger(ClubDAOImpl.class.getName());
    public static Club mapRow(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setClubId(UUID.fromString(rs.getString("clubId")));
        club.setClubName(rs.getString("clubName"));
        club.setLocation(rs.getString("location"));
        club.setHomeVenue(rs.getString("homeVenue"));
        club.setStrengthRating(rs.getInt("strengthRating"));
        club.setActive(rs.getBoolean("isActive"));
        return club;
    }
    @Override
    public Optional<Club> findByClubId(UUID clubId) {
        String query = "SELECT * FROM club WHERE clubId = ?";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, clubId.toString());
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find the Club via Club ID", e);
            throw new DataAccessException("Unable to find the Club via Club ID", e);
        }
        return Optional.empty();
    }
    @Override
    public Optional<Club> findByClubName(String clubName) {
        String query = "SELECT * FROM club WHERE clubName = ?";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, clubName);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find the Club via Club Name", e);
            throw new DataAccessException("Unable to find the Club via Club Name", e);
        }
        return Optional.empty();
    }
    @Override
    public Optional<Club> findByStrengthRating(int strengthRating) {
        String query = "SELECT * FROM club WHERE strengthRating = ?";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setInt(1, strengthRating);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find the Club via Strength Rating", e);
            throw new DataAccessException("Unable to find the Club via Strength Rating", e);
        }
        return Optional.empty();
    }
    @Override
    public List<Club> findAllClubs() {
        List<Club> clubs = new ArrayList<>();
        String query = "SELECT * FROM club";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) {
                    clubs.add(mapRow(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find the Clubs", e);
            throw new DataAccessException("Unable to find the Clubs", e);
        }
        return clubs;
    }
    @Override
    public Optional<Club> findByLocation(String location) {
        String query = "SELECT * FROM club WHERE location = ?";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, location);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to find the Club via Location", e);
            throw new DataAccessException("Unable to find the Club via Location", e);
        }
        return Optional.empty();
    }
    @Override
    public boolean createClub(Club club) {
        String query = "INSERT INTO club (clubId, clubName,location,homeVenue,strengthRating,isActive) VALUES (?,?,?,?,?,?)";
        try(Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,club.getClubId().toString());
            ps.setString(2,club.getClubName());
            ps.setString(3,club.getLocation());
            ps.setString(4,club.getHomeVenue());
            ps.setInt(5,club.getStrengthRating());
            ps.setBoolean(6,club.isActive());
            if(ps.executeUpdate()>0){
                return true;
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to create a new Club", e);
            throw new DataAccessException("Unable to create a new Club",e);
        }
        return false;
    }
    @Override
    public boolean updateClub(Club club) {
        String query = "UPDATE club SET clubName = ?, location = ?, homeVenue = ?, strengthRating = ?, isActive = ? WHERE clubId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, club.getClubName());
            ps.setString(2,club.getLocation());
            ps.setString(3,club.getHomeVenue());
            ps.setInt(4,club.getStrengthRating());
            ps.setBoolean(5,club.isActive());
            ps.setString(6,club.getClubId().toString());
            if(ps.executeUpdate()>0){
                return true;
            }
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to update the Club", e);
            throw new DataAccessException("Unable to update the club",e);
        }
        return false;
    }
    @Override
    public boolean existsByClubName(String clubName) {
        String query = "SELECT COUNT(*) club WHERE clubName = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1, clubName);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt(1) > 0;
                }
            }


        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to check duplicate club", e);
            throw new DataAccessException("Unable to check duplicate club", e);
        }
        return false;
    }

    @Override
    public boolean updateStatus(UUID clubId, boolean isActive) {
        String query = "UPDATE club SET isActive = ? WHERE clubId = ?";
        try(Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement(query)){
            ps.setBoolean(1, isActive);
            ps.setString(2, clubId.toString());
            return ps.executeUpdate()>0;
        }catch(SQLException e){
            LOG.log(Level.SEVERE,"Unable to update the Club status", e);
            throw new DataAccessException("Unable to update the Club status",e);
        }
    }

    @Override
    public boolean deactivateClub(UUID clubId) {
        return updateStatus(clubId, false);
    }

}
