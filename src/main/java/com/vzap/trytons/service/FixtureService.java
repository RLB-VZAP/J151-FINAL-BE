package com.vzap.trytons.service;

import com.vzap.trytons.dto.FixtureRequestDTO;
import com.vzap.trytons.dto.FixtureResponseDTO;
import com.vzap.trytons.enums.FixtureStatus;

import java.util.List;
import java.util.UUID;

public interface FixtureService {
    //STUB
    List<FixtureResponseDTO> listFixtures(FixtureStatus status);
    FixtureResponseDTO getFixture(UUID fixtureId);
    FixtureResponseDTO createFixture(UUID actorUserId, FixtureRequestDTO request);
    FixtureResponseDTO updateFixtureStatus(UUID actorUserId, UUID fixtureId, FixtureStatus status);
}
