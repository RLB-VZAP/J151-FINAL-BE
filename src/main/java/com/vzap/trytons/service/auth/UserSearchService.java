package com.vzap.trytons.service.auth;

import com.vzap.trytons.dto.auth.UserSearchResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserSearchService {
    List<UserSearchResponseDTO> searchUsers(UUID actorUserId, String searchTerm);
}
