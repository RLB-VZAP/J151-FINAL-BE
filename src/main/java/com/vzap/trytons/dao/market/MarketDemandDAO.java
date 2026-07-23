package com.vzap.trytons.dao.market;

import com.vzap.trytons.model.market.PlayerMarketMetrics;

import java.util.List;

public interface MarketDemandDAO {

    List<PlayerMarketMetrics> getPlayerMarketMetrics();
}
