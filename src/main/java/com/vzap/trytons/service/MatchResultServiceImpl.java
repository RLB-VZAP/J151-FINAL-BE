package com.vzap.trytons.service;

import com.vzap.trytons.dao.MatchResultDAO;
import com.vzap.trytons.dto.MatchResultRequestDTO;
import com.vzap.trytons.dto.MatchResultResponseDTO;
import com.vzap.trytons.model.MatchResult;

import java.util.Optional;
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
