package com.vzap.trytons.service;

import com.vzap.trytons.dao.UserDAO;
import com.vzap.trytons.dto.AdminUserSearchResponseDTO;
import com.vzap.trytons.dto.AdminUserStatusRequestDTO;
import com.vzap.trytons.dto.AdminUserStatusResponseDTO;
import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AdminUserServiceImpl implements AdminUserService{

    @Inject
    UserDAO userDAO;

    @Override
    public List<AdminUserSearchResponseDTO> searchUsers(UUID actorUserId, String searchTerm) {

        requireAdmin(actorUserId);

        List<User> users = userDAO.searchUsers(searchTerm);
        List<AdminUserSearchResponseDTO> searchResponses = new ArrayList<>();

        for (User user : users){
            AdminUserSearchResponseDTO response = mapToSearchResponse(user);
            searchResponses.add(response);
        }

        return searchResponses;
    }

    @Override
    public AdminUserStatusResponseDTO updateUserStatus(UUID actorUserId, UUID targetUserId, AdminUserStatusRequestDTO request) {
        return null;
    }

    private void requireAdmin(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AuthorisationException("An authenticated administrator is required to capture match results.");
        }
        User actor = userDAO.getUserById(actorUserId).orElseThrow(() -> new AuthorisationException("An authenticated administrator is required to capture match results."));
        if (actor.getRole() != UserRole.ADMINISTRATOR) {
            throw new AuthorisationException("Only administrators may capture or correct match results.");
        }
    }

    private AdminUserSearchResponseDTO mapToSearchResponse(User user) {
        return new AdminUserSearchResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getIsActive()
        );
    }
}
