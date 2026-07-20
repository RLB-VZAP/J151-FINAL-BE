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
        // TODO (W3-BE-DATABASE-LOGIC-FIX-05A): Validate the actor is an authorised administrator, re-run the simulation for the requested fixture, version the previous result, apply the approval/resimulation-count rules from MatchResultServiceImpl orchestration, and return the new ResimulationResponseDTO.
    }

    @Override
    public List<ResimulationResponseDTO> listResimulationsForFixture(UUID fixtureId) {
        // TODO (W3-BE-DATABASE-LOGIC-FIX-05A): Load the result history for the given fixture from MatchResultDAO and map each entry to a ResimulationResponseDTO.
    }
}