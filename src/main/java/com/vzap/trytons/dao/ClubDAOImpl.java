package com.vzap.trytons.dao;

import com.vzap.trytons.model.Club;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class ClubDAOImpl extends BaseDAO implements ClubDAO {
    private static final Logger LOG = Logger.getLogger(ClubDAO.class.getName());
    public static Club mapRow(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setClubId(UUID.fromString(rs.getString("club_id")));
        club.setClubName(rs.getString("club_name"));
        club.setLocation(rs.getString("location"));
        club.setHomeVenue(rs.getString("homeVenue"));
        club.setStrengthRating(rs.getInt("strengthRating"));
        club.setIsActive(rs.getBoolean("isActive"));
        return club;
    }

    @Override
    public Optional<Club> findByClubId(UUID clubId) {
        return Optional.empty();
    }

    @Override
    public Optional<Club> findByClubName(String clubName) {
        return Optional.empty();
    }

    @Override
    public Optional<Club> findByStrengthRating(int strengthRating) {
        return Optional.empty();
    }

    @Override
    public Optional<Club> findAll() {
        return Optional.empty();
    }

    @Override
    public Optional<Club> findByLocation(String location) {
        return Optional.empty();
    }

    @Override
    public boolean createClub(Club club) {
        return false;
    }

    @Override
    public boolean updateClub(Club club) {
        return false;
    }

    @Override
    public boolean existsByClubName(String clubName) {
        return false;
    }

    @Override
    public boolean deactivateClub(UUID clubId) {
        return false;
    }

    @Override
    public boolean deleteClub(Club club) {
        return false;
    }
}
