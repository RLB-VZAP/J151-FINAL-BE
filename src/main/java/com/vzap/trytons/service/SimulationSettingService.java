package com.vzap.trytons.service;
import com.vzap.trytons.dto.SimulationSettingResponseDTO;
import com.vzap.trytons.dto.SimulationSettingRequestDTO;
import java.util.UUID;

public interface SimulationSettingService {
    //STUB
    SimulationSettingResponseDTO getSettings(UUID userId, UUID leagueId);
    SimulationSettingResponseDTO updateSettings(UUID userId, SimulationSettingRequestDTO request);
}
