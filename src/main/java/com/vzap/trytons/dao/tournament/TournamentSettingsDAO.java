package com.vzap.trytons.dao.tournament;

import com.vzap.trytons.model.tournament.TournamentSettings;

import java.util.Optional;

public interface TournamentSettingsDAO {
    Optional<TournamentSettings> getSettings();

    /** Recreates the single settings row when it is missing. */
    TournamentSettings insertSettings(TournamentSettings settings);

    boolean updateSettings(TournamentSettings settings);
}
