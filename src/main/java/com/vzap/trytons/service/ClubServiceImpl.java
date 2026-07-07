package com.vzap.trytons.service;

import com.vzap.trytons.dao.ClubDAO;
import com.vzap.trytons.dto.ClubRequestDTO;
import com.vzap.trytons.dto.ClubResponseDTO;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.model.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ClubServiceImpl implements ClubService {

    private final ClubDAO clubDAO;

    @Inject
    public ClubServiceImpl(ClubDAO clubDAO) {
        this.clubDAO = clubDAO;
    }

    @Override
    public ClubResponseDTO createClub(ClubRequestDTO request) {
        validateClubRequest(request);

        String cleanClubName = request.getClubName().trim();

        if (clubDAO.existsByClubName(cleanClubName)) {
            throw new ConflictException("A club with the name '" + cleanClubName + "' already exists.");
        }

        Club club = mapRequestToClub(request);
        club.setClubId(UUID.randomUUID());
        club.setActive(request.isActive());

        boolean created = clubDAO.createClub(club);

        if (!created) {
            throw new DataAccessException("Failed to create club.", null);
        }
        return mapToResponse(club);
    }

    @Override
    public ClubResponseDTO getClub(UUID clubId) {
        validateClubId(clubId);

        Club club = clubDAO.findByClubId(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club was not found."));

        return mapToResponse(club);
    }

    @Override
    public List<ClubResponseDTO> getAllClubs() {
        return clubDAO.findAllClubs()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ClubResponseDTO updateClub(UUID clubId, ClubRequestDTO request) {

        validateClubId(clubId);
        validateClubRequest(request);

        Club existingClub = clubDAO.findByClubId(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club was not found."));

        String cleanClubName = request.getClubName().trim();

        clubDAO.findByClubName(cleanClubName).ifPresent(foundClub -> {
            if (!foundClub.getClubId().equals(existingClub.getClubId())) {
                throw new ConflictException("A club with the name '" + cleanClubName + "' already exists.");
            }
        });

        Club updatedClub = mapRequestToClub(request);
        updatedClub.setClubId(existingClub.getClubId());
        updatedClub.setActive(request.isActive());

        boolean updated = clubDAO.updateClub(updatedClub);

        if (!updated) {
            throw new DataAccessException("Failed to update club.", null);
        }
        return mapToResponse(updatedClub);
    }

    private void validateClubRequest(ClubRequestDTO request) {
        if (request == null) {
            throw new ValidationException("Club details are required.");
        }

        if (request.getClubName() == null || request.getClubName().isBlank()) {
            throw new ValidationException("Club name is required.");
        }

        if (request.getStrengthRating() < 0 || request.getStrengthRating() > 100) {
            throw new ValidationException(
                    "Club strength rating must be between 0 and 100.");
        }
    }

    private void validateClubId(UUID clubId) {
        if (clubId == null) {
            throw new ValidationException("Club ID is required.");
        }
    }

    private Club mapRequestToClub(ClubRequestDTO request) {
        Club club = new Club();

        club.setClubName(request.getClubName().trim());

        club.setLocation(
                request.getLocation() != null
                        && !request.getLocation().isBlank()
                        ? request.getLocation().trim()
                        : null);

        club.setHomeVenue(
                request.getHomeVenue() != null
                        && !request.getHomeVenue().isBlank()
                        ? request.getHomeVenue().trim()
                        : null);

        club.setStrengthRating(request.getStrengthRating());

        return club;
    }

    private ClubResponseDTO mapToResponse(Club club) {
        ClubResponseDTO response = new ClubResponseDTO();

        response.setClubId(club.getClubId());
        response.setClubName(club.getClubName());
        response.setLocation(club.getLocation());
        response.setHomeVenue(club.getHomeVenue());
        response.setStrengthRating(club.getStrengthRating());
        response.setActive(club.isActive());

        return response;
    }
}