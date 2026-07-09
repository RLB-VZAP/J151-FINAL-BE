package com.vzap.trytons.fixture.service;

import com.vzap.trytons.fixture.dto.FixtureRequestDTO;
import com.vzap.trytons.fixture.dto.FixtureResponseDTO;
import com.vzap.trytons.fixture.enums.FixtureStatus;

import java.util.List;
import java.util.UUID;

public interface FixtureService {
    //STUB
    List<FixtureResponseDTO> listFixtures(FixtureStatus status);
    FixtureResponseDTO getFixture(UUID fixtureId);
    FixtureResponseDTO createFixture(UUID actorUserId, FixtureRequestDTO request);
    FixtureResponseDTO updateFixtureStatus(UUID actorUserId, UUID fixtureId, FixtureStatus status);
}
