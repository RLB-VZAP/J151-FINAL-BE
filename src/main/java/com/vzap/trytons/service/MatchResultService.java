package com.vzap.trytons.service;

import com.vzap.trytons.dto.MatchResultRequestDTO;
import com.vzap.trytons.dto.MatchResultResponseDTO;
import java.util.UUID;

public interface MatchResultService {
    MatchResultResponseDTO captureResult(UUID actorUserId, MatchResultRequestDTO request);
    MatchResultResponseDTO getResult(UUID fixtureId);
}
