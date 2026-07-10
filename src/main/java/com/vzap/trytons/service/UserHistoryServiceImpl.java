package com.vzap.trytons.service;

import com.vzap.trytons.dto.PlayerPointsHistoryResponseDTO;
import com.vzap.trytons.dto.UserPointsHistoryResponseDTO;
import com.vzap.trytons.dto.WeeklyPerformanceResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserHistoryServiceImpl implements UserHistoryService {

    @Override
    public UserPointsHistoryResponseDTO getUserPointsHistory(String actorUserId) {
        return null;
    }

    @Override
    public List<WeeklyPerformanceResponseDTO> getWeeklyPerformance(String actorUserId) {
        return List.of();
    }
}
