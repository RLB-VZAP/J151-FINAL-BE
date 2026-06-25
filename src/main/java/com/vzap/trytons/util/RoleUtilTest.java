package com.vzap.trytons.util;

import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.model.Administrator;
import com.vzap.trytons.model.RegisteredUser;

import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public class RoleUtilTest {
    @Test
    void nullUserIsNotAdmin() {
        assertFalse(RoleUtil.isAdmin(null));
    }

    @Test
    void nullUserIsNotLoggedIn() {
        assertFalse(RoleUtil.isLoggedIn(null));
    }

    @Test
    void activeAdministratorIsAdmin() {
        Administrator admin = new Administrator();
        admin.setRole(UserRole.ADMINISTRATOR);
        admin.setIsActive(true);

        assertTrue(RoleUtil.isAdmin(admin));
        assertTrue(RoleUtil.isLoggedIn(admin));
    }

    @Test
    void inactiveAdministratorIsNotAdmin() {
        Administrator admin = new Administrator();
        admin.setRole(UserRole.ADMINISTRATOR);
        admin.setIsActive(false);

        assertFalse(RoleUtil.isAdmin(admin));
        assertFalse(RoleUtil.isLoggedIn(admin));
    }

    @Test
    void administratorWithNullIsActiveIsTreatedAsInactive() {
        Administrator admin = new Administrator();
        admin.setRole(UserRole.ADMINISTRATOR);
        admin.setIsActive(null);

        assertFalse(RoleUtil.isAdmin(admin));
    }

    @Test
    void activeRegisteredUserIsNotAdmin() {
        RegisteredUser user = new RegisteredUser();
        user.setRole(UserRole.REGISTERED_USER);
        user.setIsActive(true);

        assertFalse(RoleUtil.isAdmin(user));
        assertTrue(RoleUtil.isLoggedIn(user));
    }
}
