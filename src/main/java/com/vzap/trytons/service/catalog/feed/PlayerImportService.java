package com.vzap.trytons.service.catalog.feed;

import com.vzap.trytons.dto.catalog.PlayerImportSummaryDTO;

/**
 * Imports the external live-player feed into our catalog. Triggered on demand by
 * an administrator - a deliberate "clean data refresh", never a background poll.
 */
public interface PlayerImportService {

    /**
     * Fetches the feed, upserts players into our catalog (matched by name and club),
     * and marks feed-absent players inactive.
     *
     * @return a summary of what changed and what was skipped
     */
    PlayerImportSummaryDTO importPlayers();
}
