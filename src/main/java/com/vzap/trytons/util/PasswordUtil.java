package com.vzap.trytons.util;
import org.mindrot.jbcrypt.BCrypt;
import java.nio.charset.StandardCharsets;

public final class PasswordUtil {

    private static final int BCRYPT_WORK_FACTOR = 12;
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private PasswordUtil() {
        // Utility class: prevent instantiation.
    }

    public static String hashPassword(String rawPassword) {
        validatePasswordForHashing(rawPassword);
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    public static boolean verifyPassword(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || rawPassword.isBlank() || storedPasswordHash == null || storedPasswordHash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(rawPassword, storedPasswordHash);
        } catch (IllegalArgumentException e) {
            // Covers invalid or malformed BCrypt hashes.
            return false;
        }
    }

    private static void validatePasswordForHashing(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }

        int passwordByteLength = rawPassword.getBytes(StandardCharsets.UTF_8).length;

        if (passwordByteLength > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException("An error occurred while trying to hash password.");
        }
    }
}