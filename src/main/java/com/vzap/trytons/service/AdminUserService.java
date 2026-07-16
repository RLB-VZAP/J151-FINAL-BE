package com.vzap.trytons.service;

import com.vzap.trytons.dto.AdminUserSearchResponseDTO;
import com.vzap.trytons.dto.AdminUserStatusRequestDTO;
import com.vzap.trytons.dto.AdminUserStatusResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AdminUserService {
    public List<AdminUserSearchResponseDTO> searchUsers(UUID actorUserId, String searchTerm);
    public AdminUserStatusResponseDTO updateUserStatus(UUID actorUserId, UUID targetUserId, AdminUserStatusRequestDTO request);
}
