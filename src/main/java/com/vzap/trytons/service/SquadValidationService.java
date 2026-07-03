package com.vzap.trytons.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SquadValidationService {

    <SquadValidationResult> SquadValidationResult validateSquad(List<UUID> proposedPlayerIds, BigDecimal maximumSquadValue);
}
