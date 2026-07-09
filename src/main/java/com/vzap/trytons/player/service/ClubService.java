package com.vzap.trytons.player.service;

import com.vzap.trytons.player.dto.ClubRequestDTO;
import com.vzap.trytons.player.dto.ClubResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ClubService {
    ClubResponseDTO createClub(ClubRequestDTO request);
    ClubResponseDTO getClub(UUID clubId);
    List<ClubResponseDTO> getAllClubs();
    ClubResponseDTO updateClub(UUID clubId, ClubRequestDTO request);
}