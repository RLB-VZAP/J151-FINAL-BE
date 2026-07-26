package com.vzap.trytons.dao.pricing;

import com.vzap.trytons.model.pricing.PricingSettings;

import java.util.Optional;

public interface PricingSettingsDAO {

    Optional<PricingSettings> findSettings();

    boolean updateSettings(PricingSettings settings);
}
