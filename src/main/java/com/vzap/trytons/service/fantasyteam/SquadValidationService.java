package com.vzap.trytons.service.fantasyteam;

import com.vzap.trytons.dto.fantasyteam.SquadValidationResultDTO;
import com.vzap.trytons.enums.SquadRole;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SquadValidationService {
    SquadValidationResultDTO validateSquad(List<UUID> proposedPlayerIds);
    SquadValidationResultDTO validateSquad(List<UUID> proposedPlayerIds, List<UUID> playersRequiringAvailabilityCheck);

    /**
     * Rejects a squad whose STARTING/BENCH split is not exactly 15 STARTING
     * and 5 BENCH. A valid 20-player squad (per validateSquad's size and
     * position checks) always resolves to this split via SquadRoleAssigner,
     * so this is the final guard against a broken split reaching the
     * database rather than a routine part of every save.
     */
    SquadValidationResultDTO validateSquadRoles(Collection<SquadRole> squadRoles);

    /**
     * The fixed squad budget, on the same scale as player.value (millions of
     * rands). Shared home for the constant so fantasyteam and transfer flows
     * don't each hardcode their own copy.
     */
    BigDecimal getInitialBudget();

    /**
     * Sums live player.value reads (via PlayerDAO) for the given players.
     * Throws ResourceNotFoundException if any player does not exist.
     */
    BigDecimal computeSquadValue(List<UUID> playerIds);

    /**
     * Validates a proposed remaining-budget figure is not negative, throwing
     * BusinessRuleException with the given message if it is. Returns the same
     * value on success so callers can chain it straight into the response.
     */
    BigDecimal checkRemainingBudget(BigDecimal remainingBudget, String insufficientBudgetMessage);
}
