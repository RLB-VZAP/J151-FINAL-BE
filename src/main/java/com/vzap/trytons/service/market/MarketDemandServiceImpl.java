package com.vzap.trytons.service.market;

import com.vzap.trytons.dao.market.MarketDemandDAO;
import com.vzap.trytons.dto.market.MarketDashboardDTO;
import com.vzap.trytons.dto.market.MarketPlayerDTO;
import com.vzap.trytons.model.market.PlayerMarketMetrics;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MarketDemandServiceImpl implements MarketDemandService {

    private static final int PANEL_SIZE = 5;

    @Inject
    private MarketDemandDAO marketDemandDAO;

    @Override
    public MarketDashboardDTO getDashboard() {
        List<PlayerMarketMetrics> metrics = marketDemandDAO.getPlayerMarketMetrics();
        List<MarketPlayerDTO> players = metrics.stream().map(this::toDTO).collect(Collectors.toList());

        double medianOwnership = median(players.stream().map(p -> (double) p.getOwnershipCount()).toList());
        double medianValue = median(players.stream()
                .map(p -> p.getValue() == null ? 0.0 : p.getValue().doubleValue()).toList());

        List<MarketPlayerDTO> trending = players.stream()
                .filter(p -> p.getNetTransfers() > 0)
                .sorted(Comparator.comparingInt(MarketPlayerDTO::getNetTransfers).reversed())
                .limit(PANEL_SIZE).collect(Collectors.toList());

        List<MarketPlayerDTO> mostIn = players.stream()
                .filter(p -> p.getTransfersIn() > 0)
                .sorted(Comparator.comparingInt(MarketPlayerDTO::getTransfersIn).reversed())
                .limit(PANEL_SIZE).collect(Collectors.toList());

        List<MarketPlayerDTO> mostOut = players.stream()
                .filter(p -> p.getTransfersOut() > 0)
                .sorted(Comparator.comparingInt(MarketPlayerDTO::getTransfersOut).reversed())
                .limit(PANEL_SIZE).collect(Collectors.toList());

        List<MarketPlayerDTO> hiddenGems = players.stream()
                .filter(p -> p.getRecentPoints() > 0
                        && p.getValue() != null && p.getValue().signum() > 0
                        && p.getOwnershipCount() < medianOwnership)
                .sorted(Comparator.comparing(MarketPlayerDTO::getPointsPerValue).reversed())
                .limit(PANEL_SIZE).collect(Collectors.toList());

        List<MarketPlayerDTO> overpriced = players.stream()
                .filter(p -> p.getValue() != null && p.getValue().signum() > 0
                        && p.getValue().doubleValue() >= medianValue)
                .sorted(Comparator.comparing(MarketPlayerDTO::getPointsPerValue))
                .limit(PANEL_SIZE).collect(Collectors.toList());

        List<MarketPlayerDTO> popularCaptains = players.stream()
                .filter(p -> p.getCaptainCount() > 0)
                .sorted(Comparator.comparingInt(MarketPlayerDTO::getCaptainCount).reversed())
                .limit(PANEL_SIZE).collect(Collectors.toList());

        return MarketDashboardDTO.builder()
                .trending(trending)
                .mostTransferredIn(mostIn)
                .mostTransferredOut(mostOut)
                .hiddenGems(hiddenGems)
                .overpriced(overpriced)
                .popularCaptains(popularCaptains)
                .build();
    }

    private MarketPlayerDTO toDTO(PlayerMarketMetrics m) {
        BigDecimal value = m.getValue() == null ? BigDecimal.ZERO : m.getValue();
        BigDecimal pointsPerValue = value.signum() > 0
                ? BigDecimal.valueOf(m.getRecentPoints()).divide(value, 3, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return MarketPlayerDTO.builder()
                .playerId(m.getPlayerId())
                .playerName(m.getPlayerName())
                .value(value)
                .transfersIn(m.getTransfersIn())
                .transfersOut(m.getTransfersOut())
                .netTransfers(m.getTransfersIn() - m.getTransfersOut())
                .ownershipCount(m.getOwnershipCount())
                .recentPoints(m.getRecentPoints())
                .pointsPerValue(pointsPerValue)
                .captainCount(m.getCaptainCount())
                .build();
    }

    private double median(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        List<Double> sorted = new ArrayList<>(values);
        sorted.sort(Comparator.naturalOrder());
        int size = sorted.size();
        int mid = size / 2;
        if (size % 2 == 1) {
            return sorted.get(mid);
        }
        return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
    }
}
