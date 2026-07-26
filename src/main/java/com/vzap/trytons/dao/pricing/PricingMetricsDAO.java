package com.vzap.trytons.dao.pricing;

import com.vzap.trytons.model.pricing.PlayerPricingMetrics;

import java.util.List;

public interface PricingMetricsDAO {

    List<PlayerPricingMetrics> getMetricsForActivePlayers();
}
