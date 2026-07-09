package com.vzap.trytons.fixture.service;

import com.vzap.trytons.fixture.dto.MatchResultRequestDTO;
import com.vzap.trytons.fixture.dto.MatchResultResponseDTO;
import java.util.UUID;

public interface MatchResultService {
    MatchResultResponseDTO captureResult(UUID actorUserId, MatchResultRequestDTO request);
    MatchResultResponseDTO getResult(UUID fixtureId);
}
