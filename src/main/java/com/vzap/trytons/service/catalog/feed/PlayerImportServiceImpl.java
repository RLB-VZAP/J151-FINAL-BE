package com.vzap.trytons.service.catalog.feed;

import com.vzap.trytons.dao.catalog.ClubDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.catalog.PositionDAO;
import com.vzap.trytons.dto.catalog.PlayerFeedDTO;
import com.vzap.trytons.dto.catalog.PlayerImportSummaryDTO;
import com.vzap.trytons.model.catalog.Club;
import com.vzap.trytons.model.catalog.Player;
import com.vzap.trytons.model.catalog.Position;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class PlayerImportServiceImpl implements PlayerImportService {

    private static final Logger LOG = Logger.getLogger(PlayerImportServiceImpl.class.getName());

    @Inject
    private PlayerFeedClient playerFeedClient;

    @Inject
    private PlayerDAO playerDAO;

    @Inject
    private ClubDAO clubDAO;

    @Inject
    private PositionDAO positionDAO;

    @Override
    public PlayerImportSummaryDTO importPlayers() {
        long startNanos = System.nanoTime();

        List<PlayerFeedDTO> feed = playerFeedClient.fetchPlayers();

        Map<String, UUID> clubIdByName = loadClubIdsByName();
        Map<String, UUID> positionIdByName = loadPositionIdsByName();

        List<Player> existingPlayers = playerDAO.getAllPlayers();
        Map<String, Player> existingByKey = indexExistingPlayers(existingPlayers);

        List<Player> toInsert = new ArrayList<>();
        List<Player> toUpdate = new ArrayList<>();
        Set<UUID> matchedIds = new HashSet<>();
        int reactivated = 0;
        int skipped = 0;

        Set<String> unmappedClubIds = new LinkedHashSet<>();
        Set<String> unmappedPositionIds = new LinkedHashSet<>();
        Set<String> missingCatalogNames = new LinkedHashSet<>();

        for (PlayerFeedDTO row : feed) {
            Optional<String> clubName = ExternalCatalogMapping.clubName(row.getClubId());
            if (clubName.isEmpty()) {
                unmappedClubIds.add(row.getClubId());
                skipped++;
                continue;
            }

            Optional<String> positionName = ExternalCatalogMapping.positionName(row.getPositionId());
            if (positionName.isEmpty()) {
                unmappedPositionIds.add(row.getPositionId());
                skipped++;
                continue;
            }

            UUID clubId = clubIdByName.get(normalise(clubName.get()));
            if (clubId == null) {
                missingCatalogNames.add("club: " + clubName.get());
                skipped++;
                continue;
            }

            UUID positionId = positionIdByName.get(normalise(positionName.get()));
            if (positionId == null) {
                missingCatalogNames.add("position: " + positionName.get());
                skipped++;
                continue;
            }

            Player player = toPlayer(row, clubId, positionId);
            Player existing = existingByKey.get(matchKey(player.getPlayerName(), clubId));

            if (existing != null) {
                player.setPlayerId(existing.getPlayerId());
                toUpdate.add(player);
                matchedIds.add(existing.getPlayerId());
                if (!existing.isActive()) {
                    reactivated++;
                }
            } else {
                player.setPlayerId(UUID.randomUUID());
                toInsert.add(player);
            }
        }

        List<UUID> toDeactivate = new ArrayList<>();
        for (Player existing : existingPlayers) {
            if (existing.isActive() && !matchedIds.contains(existing.getPlayerId())) {
                toDeactivate.add(existing.getPlayerId());
            }
        }

        playerDAO.applyFeedImport(toInsert, toUpdate, toDeactivate);

        PlayerImportSummaryDTO summary = new PlayerImportSummaryDTO();
        summary.setFetched(feed.size());
        summary.setInserted(toInsert.size());
        summary.setUpdated(toUpdate.size());
        summary.setReactivated(reactivated);
        summary.setDeactivated(toDeactivate.size());
        summary.setSkipped(skipped);
        summary.setUnmappedClubIds(new ArrayList<>(unmappedClubIds));
        summary.setUnmappedPositionIds(new ArrayList<>(unmappedPositionIds));
        summary.setMissingCatalogNames(new ArrayList<>(missingCatalogNames));
        summary.setDurationMs((System.nanoTime() - startNanos) / 1_000_000);
        summary.setRefreshedAt(OffsetDateTime.now());

        LOG.log(Level.INFO,
                "Player feed import complete: fetched={0}, inserted={1}, updated={2} (reactivated={3}), "
                        + "deactivated={4}, skipped={5}.",
                new Object[]{summary.getFetched(), summary.getInserted(), summary.getUpdated(),
                        summary.getReactivated(), summary.getDeactivated(), summary.getSkipped()});

        if (!unmappedClubIds.isEmpty() || !unmappedPositionIds.isEmpty() || !missingCatalogNames.isEmpty()) {
            LOG.log(Level.WARNING,
                    "Player feed import had unmapped references: unmappedClubIds={0}, unmappedPositionIds={1}, "
                            + "missingCatalogNames={2}.",
                    new Object[]{unmappedClubIds, unmappedPositionIds, missingCatalogNames});
        }

        return summary;
    }

    private Map<String, UUID> loadClubIdsByName() {
        Map<String, UUID> byName = new HashMap<>();
        for (Club club : clubDAO.findAllClubs()) {
            byName.put(normalise(club.getClubName()), club.getClubId());
        }
        return byName;
    }

    private Map<String, UUID> loadPositionIdsByName() {
        Map<String, UUID> byName = new HashMap<>();
        for (Position position : positionDAO.findAllPositions()) {
            byName.put(normalise(position.getPositionName()), position.getPositionId());
        }
        return byName;
    }

    /**
     * Indexes existing players by (normalised name + club). Player names are not
     * globally unique in the feed (e.g. two "Sean O'Brien"s at different clubs), so
     * club is part of the identity. If our catalog already holds a duplicate for a
     * key, the first wins and the collision is logged rather than silently overwritten.
     */
    private Map<String, Player> indexExistingPlayers(List<Player> players) {
        Map<String, Player> byKey = new HashMap<>();
        for (Player player : players) {
            String key = matchKey(player.getPlayerName(), player.getClubId());
            Player previous = byKey.putIfAbsent(key, player);
            if (previous != null) {
                LOG.log(Level.WARNING,
                        "Duplicate player identity in catalog for key ''{0}''; keeping {1}, ignoring {2}.",
                        new Object[]{key, previous.getPlayerId(), player.getPlayerId()});
            }
        }
        return byKey;
    }

    private Player toPlayer(PlayerFeedDTO row, UUID clubId, UUID positionId) {
        Player player = new Player();
        player.setClubId(clubId);
        player.setPositionId(positionId);
        player.setPlayerName(row.getPlayerName().trim());
        player.setValue(row.getValue() != null ? row.getValue() : BigDecimal.ZERO);
        player.setAttackingAbility(row.getAttackingAbility());
        player.setDefensiveAbility(row.getDefensiveAbility());
        player.setKickingAbility(row.getKickingAbility());
        player.setDiscipline(row.getDiscipline());
        player.setConsistency(row.getConsistency());
        player.setFitness(row.getFitness());
        player.setCurrentForm(row.getCurrentForm());
        return player;
    }

    private String matchKey(String playerName, UUID clubId) {
        return normalise(playerName) + "|" + clubId;
    }

    private String normalise(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
