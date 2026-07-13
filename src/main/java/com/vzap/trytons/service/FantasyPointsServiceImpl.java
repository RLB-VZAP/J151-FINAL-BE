package com.vzap.trytons.service;

import com.vzap.trytons.dto.FantasyPointsRequestDTO;
import com.vzap.trytons.dto.FantasyPointsResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FantasyPointsServiceImpl implements FantasyPointsService {

    @Override
    public FantasyPointsResponseDTO calculateFantasyPoints(UUID actorUserId, FantasyPointsRequestDTO request) {

        throw new UnsupportedOperationException("FantasyPointsServiceImpl.calculateFantasyPoints is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after scoring-rule lookup, point calculation, versioning, and breakdown rules are confirmed.");
    }

    @Override
    public FantasyPointsResponseDTO getFantasyPointsById(UUID pointsId) {

        throw new UnsupportedOperationException("FantasyPointsServiceImpl.getFantasyPointsById is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointsDAO read methods and response mapping are confirmed.");
    }

    @Override
    public List<FantasyPointsResponseDTO> listFantasyPointsForStat(UUID statId) {

        throw new UnsupportedOperationException("FantasyPointsServiceImpl.listFantasyPointsForStat is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointsDAO stat-based lookup and response mapping are confirmed.");
    }

    @Override
    public FantasyPointsResponseDTO getFinalFantasyPointsForStat(UUID statId) {

        throw new UnsupportedOperationException("FantasyPointsServiceImpl.getFinalFantasyPointsForStat is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after final-version selection rules are confirmed.");
    }
}