package com.vzap.trytons.service;

import com.vzap.trytons.dto.FantasyPointBreakdownResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FantasyPointBreakdownServiceImpl implements FantasyPointBreakdownService {

    @Override
    public FantasyPointBreakdownResponseDTO getBreakdownById(UUID breakdownId) {

        throw new UnsupportedOperationException("FantasyPointBreakdownServiceImpl.getBreakdownById is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointBreakdownDAO read mapping is confirmed.");
    }

    @Override
    public List<FantasyPointBreakdownResponseDTO> listBreakdownsForPoints(UUID pointsId) {

        throw new UnsupportedOperationException("FantasyPointBreakdownServiceImpl.listBreakdownsForPoints is a stub for W3-BE-DATABASE-LOGIC-FIX-05A. " + "Implement after FantasyPointBreakdownDAO points-based lookup and response mapping are confirmed.");
    }
}