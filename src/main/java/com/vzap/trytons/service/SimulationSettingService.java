package com.vzap.trytons.service;

import com.vzap.trytons.dto.SimulationSettingRequestDTO;
import com.vzap.trytons.dto.SimulationSettingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SimulationSettingService {
    SimulationSettingResponseDTO createSimulationSetting(UUID actorUserId, SimulationSettingRequestDTO request);
    SimulationSettingResponseDTO updateSimulationSetting(UUID actorUserId, UUID simulationSettingsId, SimulationSettingRequestDTO request);
    SimulationSettingResponseDTO getSimulationSettingById(UUID simulationSettingsId);
    SimulationSettingResponseDTO getActiveSimulationSetting();
    List<SimulationSettingResponseDTO> listSimulationSettings();
}