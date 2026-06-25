package com.vzap.trytons.util;

import com.vzap.trytons.enums.UserRole;
import com.vzap.trytons.model.Administrator;
import com.vzap.trytons.model.RegisteredUser;
import com.vzap.trytons.model.User;

public class RoleUtil {
    private RoleUtil() {}

    private static boolean isActive (User user){
        return Boolean.TRUE.equals(user.getIsActive());
    }

    public static boolean isAdmin (User user){
        if (user == null) return false;
        if (!isActive(user)) return false;
        return user.getRole() == UserRole.ADMINISTRATOR;
    }

    public static boolean isLoggedIn (User user){
        return user != null && isActive(user);
    }


}
