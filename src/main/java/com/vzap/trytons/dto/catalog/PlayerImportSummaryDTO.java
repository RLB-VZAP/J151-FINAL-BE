package com.vzap.trytons.dto.catalog;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Outcome of a single player-feed import run, returned to the administrator who
 * triggered it. Surfaces exactly what changed and what was skipped so a "clean
 * data refresh" is auditable rather than opaque.
 */
@Getter
@Setter
public class PlayerImportSummaryDTO {
    /** Total player rows received from the feed. */
    private int fetched;
    /** New players created in our catalog. */
    private int inserted;
    /** Existing players matched by (name + club) and refreshed. */
    private int updated;
    /** Of the updated players, how many were previously inactive and are now active again. */
    private int reactivated;
    /** Players present in our catalog but absent from the feed, marked inactive. */
    private int deactivated;
    /** Feed rows skipped because their club or position UUID could not be resolved. */
    private int skipped;

    /** Distinct feed club UUIDs with no entry in the external-catalog mapping. */
    private List<String> unmappedClubIds = new ArrayList<>();
    /** Distinct feed position UUIDs with no entry in the external-catalog mapping. */
    private List<String> unmappedPositionIds = new ArrayList<>();
    /** Catalog names the mapping expected but which are missing from our club/position tables. */
    private List<String> missingCatalogNames = new ArrayList<>();

    private long durationMs;
    private OffsetDateTime refreshedAt;
}
