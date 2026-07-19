package com.vzap.trytons.service.simulation;

import com.vzap.trytons.dto.simulation.ResimulationRequestDTO;
import com.vzap.trytons.dto.simulation.ResimulationResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ControlledResimulationServiceImpl implements ControlledResimulationService {

    @Override
    public ResimulationResponseDTO resimulateFixture(UUID actorUserId, ResimulationRequestDTO request) {
        throw new UnsupportedOperationException("ControlledResimulationServiceImpl.resimulateFixture is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchResultServiceImpl orchestration, result versioning, and approval rules are confirmed.");
    }

    @Override
    public List<ResimulationResponseDTO> listResimulationsForFixture(UUID fixtureId) {
        throw new UnsupportedOperationException("ControlledResimulationServiceImpl.listResimulationsForFixture is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after MatchResultDAO result-history methods are ready.");
    }
}