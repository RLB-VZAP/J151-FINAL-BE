package com.vzap.trytons.dao;

import com.vzap.trytons.model.Admin;

import java.util.Optional;
import java.util.UUID;

public interface AdminDAO {
    Optional<Admin> getAdminById(UUID userId);

    boolean deactivateUserAccount(UUID userId);
}
