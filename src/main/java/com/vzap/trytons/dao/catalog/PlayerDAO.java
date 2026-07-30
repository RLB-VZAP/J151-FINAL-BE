package com.vzap.trytons.dao.catalog;

import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PlayerDAO {

    Optional<Player> getPlayerById(UUID playerId);
    List<Player> getAllPlayers();

    /**
     * The current availability status of many players in one query, so a catalogue
     * page does not turn into one lookup per row. Players with no availability
     * record are absent from the result rather than mapped to a guessed status —
     * the caller decides what a missing record means.
     */
    Map<UUID, AvailabilityStatus> getCurrentAvailabilityStatuses(Collection<UUID> playerIds);

    List<Player> searchPlayers(String playerName, UUID clubId, UUID positionId, BigDecimal minValue, BigDecimal maxValue, Integer minCurrentForm, Integer maxCurrentForm, AvailabilityStatus availabilityStatus, Boolean isActive);
    Optional<Player> createPlayer(Player player);
    Optional<Player> updatePlayer(Player player);
    boolean updateValue(UUID playerId, BigDecimal newValue);
    boolean deactivatePlayer(UUID playerId);
    List<Player> getPlayersByClubId(UUID clubId);
    List<Player> getPlayersByPositionId(UUID positionId);
    Optional<PlayerAvailability> getCurrentAvailability(UUID playerId);
    List<PlayerAvailability> getAvailabilityHistory(UUID playerId);
}