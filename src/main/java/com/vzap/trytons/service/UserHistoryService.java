package com.vzap.trytons.service;

import com.vzap.trytons.dto.UserPointsHistoryResponseDTO;
import com.vzap.trytons.dto.WeeklyPerformanceResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserHistoryService {

    UserPointsHistoryResponseDTO getUserPointsHistory(UUID actorUserId);
    List<WeeklyPerformanceResponseDTO> getWeeklyPerformance(UUID actorUserId);
}