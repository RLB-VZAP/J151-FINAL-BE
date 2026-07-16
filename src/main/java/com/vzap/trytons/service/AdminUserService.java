package com.vzap.trytons.service;

import com.vzap.trytons.dto.AdminUserSearchResponseDTO;
import com.vzap.trytons.dto.AdminUserStatusRequestDTO;
import com.vzap.trytons.dto.AdminUserStatusResponseDTO;

import java.util.List;

public interface AdminUserService {
    public List<AdminUserSearchResponseDTO> searchUsers(String actorUserId, String searchTerm);
    public AdminUserStatusResponseDTO updateUserStatus(String actorUserId, String targetUserId, AdminUserStatusRequestDTO request);
}
