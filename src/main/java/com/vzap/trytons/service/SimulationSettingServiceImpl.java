package com.vzap.trytons.service;

import com.vzap.trytons.dto.SimulationSettingRequestDTO;
import com.vzap.trytons.dto.SimulationSettingResponseDTO;

import java.util.UUID;

public class SimulationSettingServiceImpl implements SimulationSettingService {

    @Override
    public SimulationSettingResponseDTO getSettings(UUID userId, UUID leagueId) {
        return null;
    }

    @Override
    public SimulationSettingResponseDTO updateSettings(UUID userId, SimulationSettingRequestDTO request) {
        return null;
    }
}
