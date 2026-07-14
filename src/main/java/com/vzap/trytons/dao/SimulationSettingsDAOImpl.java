package com.vzap.trytons.dao;

import com.vzap.trytons.model.SimulationSettings;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class SimulationSettingsDAOImpl implements SimulationSettingsDAO {

    @Override
    public SimulationSettings save(SimulationSettings simulationSettings) {
        throw new UnsupportedOperationException("SimulationSettingsDAOImpl.save is a stub.");
    }

    @Override
    public Optional<SimulationSettings> findById(UUID settingsId) {
        throw new UnsupportedOperationException("SimulationSettingsDAOImpl.findById is a stub.");
    }

    @Override
    public Optional<SimulationSettings> findActive() {
        throw new UnsupportedOperationException("SimulationSettingsDAOImpl.findActive is a stub.");
    }

    @Override
    public List<SimulationSettings> findAll() {
        throw new UnsupportedOperationException("SimulationSettingsDAOImpl.findAll is a stub.");
    }

    @Override
    public int markAllSettingsInactive() {
        throw new UnsupportedOperationException("SimulationSettingsDAOImpl.markAllSettingsInactive is a stub.");
    }
}