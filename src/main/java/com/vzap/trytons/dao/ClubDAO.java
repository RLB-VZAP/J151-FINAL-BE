package com.vzap.trytons.dao;

import com.vzap.trytons.model.Club;

import java.util.Optional;
import java.util.UUID;
public interface ClubDAO {
    Optional<Club> findByClubId(UUID clubId);
    Optional<Club> findByClubName(String clubName);
    Optional<Club>findByStrengthRating(int strengthRating);
    Optional<Club>findAll();
    Optional<Club> findByLocation(String location);
    public boolean createClub(Club club);
    public boolean updateClub(Club club);
    public boolean existsByClubName(String clubName);
    public boolean deactivateClub(UUID clubId);
    public boolean deleteClub(Club club);


}
