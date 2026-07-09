package com.vzap.trytons.shared.config;

import io.github.cdimascio.dotenv.Dotenv;

public class DotEnvConfig {
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private DotEnvConfig() {
    }

    public static String getRequired(String key) {
        String value = DOTENV.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }

        return value;
    }

    public static long getRequiredLong(String key) {
        String value = getRequired(key);

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(key + " must contain a valid whole number.", e);
        }
    }

    public static int getRequiredInt(String key) {
        String value = getRequired(key);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(key + " must contain a valid integer.", e);
        }
    }
}
