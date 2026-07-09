package com.vzap.trytons.auth.dao;

import com.vzap.trytons.auth.model.Administrator;

import java.util.Optional;
import java.util.UUID;

public interface AdministratorDAO {
    Optional<Administrator> getAdministratorById(UUID userId);

    boolean deactivateUserAccount(UUID userId);
}
