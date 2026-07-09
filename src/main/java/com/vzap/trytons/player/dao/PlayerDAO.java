package com.vzap.trytons.player.dao;

import com.vzap.trytons.player.enums.AvailabilityStatus;
import com.vzap.trytons.player.model.Player;
import com.vzap.trytons.player.model.PlayerAvailability;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerDAO {

    Optional<Player> getPlayerById(UUID playerId);
    List<Player> getAllPlayers();
    List<Player> searchPlayers(
            String playerName,
            UUID clubId,
            UUID positionId,
            BigDecimal minValue,
            BigDecimal maxValue,
            Integer minTotalFantasyPoints,
            Integer maxTotalFantasyPoints,
            Integer minCurrentForm,
            Integer maxCurrentForm,
            AvailabilityStatus availabilityStatus,
            Boolean isActive);

    Optional<Player> createPlayer(Player player);
    Optional<Player> updatePlayer(Player player);
    boolean deactivatePlayer(UUID playerId);
    List<Player> getPlayersByClubId(UUID clubId);
    List<Player> getPlayersByPositionId(UUID positionId);
    Optional<PlayerAvailability> getCurrentAvailability(UUID playerId);
    List<PlayerAvailability> getAvailabilityHistory(UUID playerId);
}