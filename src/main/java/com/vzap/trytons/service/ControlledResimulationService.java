package com.vzap.trytons.service;

import java.util.*;
import java.util.UUID;
import com.vzap.trytons.dto.*;
import java.util.*;
import java.util.UUID;

public interface ControlledResimulationService {
    ResimulationResponseDTO requestResimulation(UUID actorUserId, ResimulationRequestDTO request);
}