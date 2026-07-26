package com.vzap.trytons.service.pricing;

import com.vzap.trytons.dto.pricing.PlayerPriceHistoryDTO;
import com.vzap.trytons.dto.pricing.PricingRunSummaryDTO;
import com.vzap.trytons.dto.pricing.PricingSettingsDTO;

import java.util.List;
import java.util.UUID;

public interface PricingService {

    PricingSettingsDTO getSettings(UUID actorUserId);

    PricingSettingsDTO updateSettings(UUID actorUserId, PricingSettingsDTO request);

    /** Compute price changes without persisting them (admin preview). */
    PricingRunSummaryDTO preview(UUID actorUserId);

    /** Compute and persist price changes (admin manual run). */
    PricingRunSummaryDTO apply(UUID actorUserId, String reason);

    /**
     * System-triggered recalculation (e.g. after round processing). Applies
     * changes without an admin actor and never throws to its caller.
     */
    PricingRunSummaryDTO recalculateAll(String reason);

    List<PlayerPriceHistoryDTO> getPlayerHistory(UUID playerId, int limit);
}
