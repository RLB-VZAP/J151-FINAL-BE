package com.vzap.trytons.dao;

import com.vzap.trytons.model.Administrator;

import java.util.Optional;
import java.util.UUID;

public interface AdminDAO {
    Optional<Administrator> getAdministratorById(UUID userId);

    boolean deactivateUserAccount(UUID userId);
}
