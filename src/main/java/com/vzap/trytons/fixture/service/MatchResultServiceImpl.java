package com.vzap.trytons.fixture.service;

import com.vzap.trytons.fixture.dto.MatchResultRequestDTO;
import com.vzap.trytons.fixture.dto.MatchResultResponseDTO;

import java.util.UUID;

public class MatchResultServiceImpl implements MatchResultService {
    @Override
    public MatchResultResponseDTO captureResult(UUID actorUserId, MatchResultRequestDTO request) {
        return null;
    }
    @Override
    public MatchResultResponseDTO getResult(UUID fixtureId) {
        return null;
    }
}
