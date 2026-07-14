package com.vzap.trytons.dao;

import com.vzap.trytons.model.SimulationSettings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulationSettingsDAO {
    SimulationSettings save(SimulationSettings simulationSettings);
    Optional<SimulationSettings> findById(UUID settingsId);
    Optional<SimulationSettings> findActive();
    List<SimulationSettings> findAll();
    int markAllSettingsInactive();
}