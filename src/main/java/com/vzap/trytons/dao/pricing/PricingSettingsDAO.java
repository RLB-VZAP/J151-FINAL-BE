package com.vzap.trytons.dao.pricing;

import com.vzap.trytons.model.pricing.PricingSettings;

import java.util.Optional;

public interface PricingSettingsDAO {

    Optional<PricingSettings> findSettings();

    boolean updateSettings(PricingSettings settings);

    /**
     * Inserts a brand-new settings row. Used to recover when the seeded
     * single row is missing, so pricing has something to load rather than
     * throwing forever.
     */
    boolean insertSettings(PricingSettings settings);
}
