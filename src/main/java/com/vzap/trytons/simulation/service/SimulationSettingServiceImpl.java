package com.vzap.trytons.simulation.service;

import com.vzap.trytons.simulation.dto.SimulationSettingRequestDTO;
import com.vzap.trytons.simulation.dto.SimulationSettingResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SimulationSettingServiceImpl implements SimulationSettingService {

    @Override
    public SimulationSettingResponseDTO createSimulationSetting(UUID actorUserId, SimulationSettingRequestDTO request) {
        throw new UnsupportedOperationException("SimulationSettingServiceImpl.createSimulationSetting is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after simulation settings DAO contract and validation rules are confirmed.");
    }

    @Override
    public SimulationSettingResponseDTO updateSimulationSetting(UUID actorUserId, UUID simulationSettingsId, SimulationSettingRequestDTO request) {
        throw new UnsupportedOperationException("SimulationSettingServiceImpl.updateSimulationSetting is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after simulation settings DAO contract and validation rules are confirmed.");
    }

    @Override
    public SimulationSettingResponseDTO getSimulationSettingById(UUID simulationSettingsId) {
        throw new UnsupportedOperationException("SimulationSettingServiceImpl.getSimulationSettingById is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after simulation settings DAO contract and validation rules are confirmed.");
    }

    @Override
    public SimulationSettingResponseDTO getActiveSimulationSetting() {
        throw new UnsupportedOperationException("SimulationSettingServiceImpl.getActiveSimulationSetting is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after simulation settings DAO contract and validation rules are confirmed.");
    }

    @Override
    public List<SimulationSettingResponseDTO> listSimulationSettings() {
        throw new UnsupportedOperationException("SimulationSettingServiceImpl.listSimulationSettings is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after simulation settings DAO contract and validation rules are confirmed.");
    }
}