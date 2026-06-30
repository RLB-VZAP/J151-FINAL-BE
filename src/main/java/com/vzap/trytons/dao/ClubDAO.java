package com.vzap.trytons.dao;

import com.vzap.trytons.model.Club;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@SuppressWarnings("ALL")
public interface ClubDAO {
    Optional<Club> findByClubId(UUID clubId);
    Optional<Club> findByClubName(String clubName);
    Optional<Club>findByStrengthRating(int strengthRating);
    List<Club> findAllClubs();
    Optional<Club> findByLocation(String location);
    public boolean createClub(Club club);
    public boolean updateClub(Club club);
    public boolean existsByClubName(String clubName);
    public boolean updateStatus(UUID clubId, boolean isActive);
    public boolean deactivateClub(UUID clubId);



}
