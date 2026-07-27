package com.vzap.trytons.service.auth;

import com.vzap.trytons.dao.auth.UserDAO;
import com.vzap.trytons.dto.auth.UserSearchResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.model.auth.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserSearchServiceImpl implements UserSearchService {

    // A 1-character wildcard would let a caller enumerate most of the user
    // base one letter at a time, so a search term must clear this length first.
    private static final int MIN_SEARCH_TERM_LENGTH = 2;

    // Caps how many matches the messaging "find someone" search can return.
    private static final int MAX_SEARCH_RESULTS = 20;

    @Inject
    private UserDAO userDAO;

    @Override
    public List<UserSearchResponseDTO> searchUsers(UUID actorUserId, String searchTerm) {
        if (actorUserId == null) {
            throw new AuthenticationException("Authentication required.");
        }

        // A blank or too-short term never falls back to "list everyone" — that
        // would hand any logged-in user the whole directory.
        if (searchTerm == null || searchTerm.trim().length() < MIN_SEARCH_TERM_LENGTH) {
            return List.of();
        }

        List<User> users = userDAO.searchActiveUsersByUsername(searchTerm.trim(), actorUserId, MAX_SEARCH_RESULTS);
        List<UserSearchResponseDTO> searchResponses = new ArrayList<>();

        for (User user : users) {
            searchResponses.add(mapToSearchResponse(user));
        }

        return searchResponses;
    }

    private UserSearchResponseDTO mapToSearchResponse(User user) {
        return UserSearchResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .build();
    }
}
