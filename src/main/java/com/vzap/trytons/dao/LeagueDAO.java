package com.vzap.trytons.dao;
import com.vzap.trytons.model.League;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueDAO {
   League saveLeague(League league);
    Optional<League> findLeagueById(UUID leagueId);
    List<League> findAllLeagues();
    List<League> findLeaguesByLeagueManager(UUID userId);//could also be with their name
    Optional<League> findLeagueByLeagueCode(UUID leagueCode);
    boolean existsByLeagueCode(String leagueCode);
    boolean deactivateLeague(UUID leagueId);
    League updateLeague(League league);
    boolean deleteLeague(UUID leagueId);
}