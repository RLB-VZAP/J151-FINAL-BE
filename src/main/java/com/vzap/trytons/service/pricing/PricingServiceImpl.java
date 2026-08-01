package com.vzap.trytons.service.pricing;

import com.vzap.trytons.dao.admin.AdminDAO;
import com.vzap.trytons.dao.catalog.PlayerDAO;
import com.vzap.trytons.dao.pricing.PlayerPriceHistoryDAO;
import com.vzap.trytons.dao.pricing.PricingMetricsDAO;
import com.vzap.trytons.dao.pricing.PricingSettingsDAO;
import com.vzap.trytons.dto.pricing.PlayerPriceHistoryDTO;
import com.vzap.trytons.dto.pricing.PriceChangeDTO;
import com.vzap.trytons.dto.pricing.PricingRunSummaryDTO;
import com.vzap.trytons.dto.pricing.PricingSettingsDTO;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.pricing.PlayerPriceHistory;
import com.vzap.trytons.model.pricing.PlayerPricingMetrics;
import com.vzap.trytons.model.pricing.PricingSettings;
import com.vzap.trytons.util.PricingCalculator;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PricingServiceImpl implements PricingService {

    private static final Logger LOG = Logger.getLogger(PricingServiceImpl.class.getName());

    @Inject
    private PricingMetricsDAO pricingMetricsDAO;
    @Inject
    private PricingSettingsDAO pricingSettingsDAO;
    @Inject
    private PlayerPriceHistoryDAO priceHistoryDAO;
    @Inject
    private PlayerDAO playerDAO;
    @Inject
    private AdminDAO adminDAO;

    @Override
    public PricingSettingsDTO getSettings(UUID actorUserId) {
        requireAdmin(actorUserId);
        return toSettingsDTO(loadSettings());
    }

    @Override
    public PricingSettingsDTO updateSettings(UUID actorUserId, PricingSettingsDTO request) {
        requireAdmin(actorUserId);
        if (request == null) {
            throw new ValidationException("Pricing settings are required.");
        }

        PricingSettings settings = loadSettings();
        settings.setWeightForm(requireWeight(request.getWeightForm(), "form"));
        settings.setWeightPopularity(requireWeight(request.getWeightPopularity(), "popularity"));
        settings.setWeightPoints(requireWeight(request.getWeightPoints(), "points"));
        settings.setWeightInjury(requireWeight(request.getWeightInjury(), "injury"));
        settings.setWeightDemand(requireWeight(request.getWeightDemand(), "demand"));
        settings.setWeightAvailability(requireWeight(request.getWeightAvailability(), "availability"));

        BigDecimal maxDelta = request.getMaxDeltaPct();
        if (maxDelta == null || maxDelta.signum() <= 0) {
            throw new ValidationException("Maximum change per run must be greater than zero.");
        }
        settings.setMaxDeltaPct(maxDelta);

        BigDecimal minValue = request.getMinValue();
        BigDecimal maxValue = request.getMaxValue();
        if (minValue == null || minValue.signum() < 0) {
            throw new ValidationException("Minimum value must be zero or greater.");
        }
        if (maxValue == null || maxValue.compareTo(minValue) <= 0) {
            throw new ValidationException("Maximum value must be greater than the minimum value.");
        }
        settings.setMinValue(minValue);
        settings.setMaxValue(maxValue);

        pricingSettingsDAO.updateSettings(settings);
        return toSettingsDTO(loadSettings());
    }

    @Override
    public PricingRunSummaryDTO preview(UUID actorUserId) {
        requireAdmin(actorUserId);
        return run(loadSettings(), false, null);
    }

    @Override
    public PricingRunSummaryDTO apply(UUID actorUserId, String reason) {
        requireAdmin(actorUserId);
        return run(loadSettings(), true, reason == null ? "Manual admin run" : reason);
    }

    @Override
    public PricingRunSummaryDTO recalculateAll(String reason) {
        try {
            return run(loadSettings(), true, reason == null ? "Automatic: round processing" : reason);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Automatic price recalculation failed", e);
            return PricingRunSummaryDTO.builder()
                    .runAt(LocalDateTime.now())
                    .applied(true)
                    .playersEvaluated(0)
                    .playersRepriced(0)
                    .totalIncrease(BigDecimal.ZERO)
                    .totalDecrease(BigDecimal.ZERO)
                    .changes(new ArrayList<>())
                    .build();
        }
    }

    @Override
    public List<PlayerPriceHistoryDTO> getPlayerHistory(UUID playerId, int limit) {
        if (playerId == null) {
            throw new ValidationException("A player is required.");
        }
        List<PlayerPriceHistoryDTO> history = new ArrayList<>();
        for (PlayerPriceHistory row : priceHistoryDAO.findByPlayer(playerId, limit)) {
            history.add(PlayerPriceHistoryDTO.builder()
                    .oldValue(row.getOldValue())
                    .newValue(row.getNewValue())
                    .delta(row.getDelta())
                    .reason(row.getReason())
                    .createdAt(row.getCreatedAt())
                    .build());
        }
        return history;
    }

    // ----- core computation -----

    private PricingRunSummaryDTO run(PricingSettings settings, boolean applyChanges, String reason) {
        List<PlayerPricingMetrics> metrics = pricingMetricsDAO.getMetricsForActivePlayers();

        int maxOwnership = 0;
        int maxAbsDemand = 0;
        int maxPoints = 0;
        for (PlayerPricingMetrics m : metrics) {
            maxOwnership = Math.max(maxOwnership, m.getOwnershipCount());
            maxAbsDemand = Math.max(maxAbsDemand, Math.abs(m.getNetTransferDemand()));
            maxPoints = Math.max(maxPoints, m.getRecentFantasyPoints());
        }

        List<PriceChangeDTO> changes = new ArrayList<>();
        BigDecimal totalIncrease = BigDecimal.ZERO;
        BigDecimal totalDecrease = BigDecimal.ZERO;

        for (PlayerPricingMetrics m : metrics) {
            BigDecimal oldValue = m.getCurrentValue();
            BigDecimal newValue = PricingCalculator.computeNewValue(settings, m, maxOwnership, maxAbsDemand, maxPoints);
            if (newValue.compareTo(oldValue) == 0) {
                continue;
            }

            BigDecimal delta = newValue.subtract(oldValue);
            changes.add(PriceChangeDTO.builder()
                    .playerId(m.getPlayerId())
                    .playerName(m.getPlayerName())
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .delta(delta)
                    .build());

            if (delta.signum() > 0) {
                totalIncrease = totalIncrease.add(delta);
            } else {
                totalDecrease = totalDecrease.add(delta.abs());
            }

            if (applyChanges) {
                persistChange(m.getPlayerId(), oldValue, newValue, reason);
            }
        }

        return PricingRunSummaryDTO.builder()
                .runAt(LocalDateTime.now())
                .applied(applyChanges)
                .playersEvaluated(metrics.size())
                .playersRepriced(changes.size())
                .totalIncrease(totalIncrease)
                .totalDecrease(totalDecrease)
                .changes(changes)
                .build();
    }

    private void persistChange(UUID playerId, BigDecimal oldValue, BigDecimal newValue, String reason) {
        try {
            playerDAO.updateValue(playerId, newValue);
            priceHistoryDAO.create(PlayerPriceHistory.builder()
                    .playerId(playerId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .reason(reason)
                    .build());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Unable to apply price change for player " + playerId, e);
        }
    }

    // ----- helpers -----

    private PricingSettings loadSettings() {
        return pricingSettingsDAO.findSettings()
                .orElseGet(this::createDefaultSettings);
    }

    /**
     * Recovers from the seeded pricing_settings row being missing (schema.sql
     * expects exactly one row, but nothing guarantees it stays that way).
     * Without this, both the admin pricing screen and the automatic
     * per-round reprice would throw forever - the latter silently, since the
     * caller in CompetitionProcessingServiceImpl swallows pricing failures.
     * Defaults mirror the column DEFAULTs in schema.sql exactly.
     */
    private PricingSettings createDefaultSettings() {
        PricingSettings defaults = PricingSettings.builder()
                .settingsId(UUID.randomUUID())
                .weightForm(new BigDecimal("0.1000"))
                .weightPopularity(new BigDecimal("0.0500"))
                .weightPoints(new BigDecimal("0.1000"))
                .weightInjury(new BigDecimal("0.1500"))
                .weightDemand(new BigDecimal("0.0800"))
                .weightAvailability(new BigDecimal("0.2000"))
                .maxDeltaPct(new BigDecimal("0.1500"))
                .minValue(new BigDecimal("1.00"))
                .maxValue(new BigDecimal("300.00"))
                .build();

        pricingSettingsDAO.insertSettings(defaults);

        return pricingSettingsDAO.findSettings()
                .orElseThrow(() -> new ResourceNotFoundException("Pricing settings have not been configured."));
    }

    private BigDecimal requireWeight(BigDecimal weight, String name) {
        if (weight == null) {
            throw new ValidationException("A weight for " + name + " is required.");
        }
        if (weight.signum() < 0) {
            throw new ValidationException("The weight for " + name + " must be zero or greater.");
        }
        return weight;
    }

    private PricingSettingsDTO toSettingsDTO(PricingSettings settings) {
        return PricingSettingsDTO.builder()
                .weightForm(settings.getWeightForm())
                .weightPopularity(settings.getWeightPopularity())
                .weightPoints(settings.getWeightPoints())
                .weightInjury(settings.getWeightInjury())
                .weightDemand(settings.getWeightDemand())
                .weightAvailability(settings.getWeightAvailability())
                .maxDeltaPct(settings.getMaxDeltaPct())
                .minValue(settings.getMinValue())
                .maxValue(settings.getMaxValue())
                .build();
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null || adminDAO.getAdminById(actorUserId).isEmpty()) {
            throw new AuthorisationException("Only administrators may manage player pricing.");
        }
    }
}
