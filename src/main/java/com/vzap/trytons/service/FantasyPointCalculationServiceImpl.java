package com.vzap.trytons.service;

import com.vzap.trytons.dao.FixtureDAO;
import com.vzap.trytons.dao.PlayerStatisticsDAO;
import com.vzap.trytons.dto.FantasyPointCalculationResultDTO;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.Fixture;
import com.vzap.trytons.model.PlayerStatistics;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FantasyPointCalculationServiceImpl implements FantasyPointCalculationService {

    @Inject
    FixtureDAO fixtureDAO;

    @Inject
    PlayerStatisticsDAO playerStatisticsDAO;

    @Override
    public FantasyPointCalculationResultDTO calculateForFixture(String fixtureId) {
        UUID currentFixtureId = UUID.fromString(fixtureId);

        try {
            Optional<Fixture> currentFixture = fixtureDAO.findById(currentFixtureId);
            currentFixture.getLeagueId.getLeagueId;

        } catch (Exception e) {
            throw new ResourceNotFoundException("Could not find the fixture from the given fixture ID");
        }

        List<PlayerStatistics> playerStatistics = playerStatisticsDAO.findByFixtureId(currentFixtureId);


    }
}
