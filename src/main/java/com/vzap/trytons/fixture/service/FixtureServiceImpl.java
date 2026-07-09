package com.vzap.trytons.fixture.service;

import com.vzap.trytons.fixture.dto.FixtureRequestDTO;
import com.vzap.trytons.fixture.dto.FixtureResponseDTO;
import com.vzap.trytons.fixture.enums.FixtureStatus;

import java.util.List;
import java.util.UUID;

public class FixtureServiceImpl implements FixtureService {
    //STUB
    @Override
    public List<FixtureResponseDTO> listFixtures(FixtureStatus status) {
        return List.of();
    }

    @Override
    public FixtureResponseDTO getFixture(UUID fixtureId) {
        return null;
    }

    @Override
    public FixtureResponseDTO createFixture(UUID actorUserId, FixtureRequestDTO request) {
        return null;
    }

    @Override
    public FixtureResponseDTO updateFixtureStatus(UUID actorUserId, UUID fixtureId, FixtureStatus status) {
        return null;
    }
}
