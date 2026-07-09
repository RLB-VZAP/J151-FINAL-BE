package com.vzap.trytons.auth.util;

import com.vzap.trytons.auth.annotation.AdminOnly;
import com.vzap.trytons.auth.enums.UserRole;
import com.vzap.trytons.auth.model.User;
import com.vzap.trytons.security.AuthPrincipal;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;

@Provider
@AdminOnly
@Priority(Priorities.AUTHORIZATION)
public class RoleUtil {
    private RoleUtil() {}

    private static boolean isActive (User user){
        return Boolean.TRUE.equals(user.getIsActive());
    }

    public static boolean isAdmin (AuthPrincipal principal){
        if (principal == null) return false;
        return principal.getRole() == UserRole.ADMINISTRATOR;
    }



    public static boolean isLoggedIn (User user){
        return user != null && isActive(user);
    }
}
