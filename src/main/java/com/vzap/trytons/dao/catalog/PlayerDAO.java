package com.vzap.trytons.dao.catalog;

import com.vzap.trytons.enums.AvailabilityStatus;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.PlayerAvailability;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerDAO {

    Optional<Player> getPlayerById(UUID playerId);
    List<Player> getAllPlayers();

    /**
     * Applies a live-feed import as a single atomic transaction: inserts new players,
     * updates matched players (also reactivating any that had been marked inactive),
     * and deactivates players absent from the feed. All or nothing - any failure rolls
     * the whole batch back.
     *
     * @param toInsert       players to create (each must already carry a playerId)
     * @param toUpdate       players to overwrite in place (matched by playerId)
     * @param idsToDeactivate playerIds to set isActive = FALSE
     */
    void applyFeedImport(Collection<Player> toInsert, Collection<Player> toUpdate, Collection<UUID> idsToDeactivate);

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