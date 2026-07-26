package com.vzap.trytons.dao.pricing;

import com.vzap.trytons.model.pricing.PlayerPriceHistory;

import java.util.List;
import java.util.UUID;

public interface PlayerPriceHistoryDAO {

    PlayerPriceHistory create(PlayerPriceHistory history);

    List<PlayerPriceHistory> findByPlayer(UUID playerId, int limit);
}
