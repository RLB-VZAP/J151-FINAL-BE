package com.vzap.trytons.service.message;

import java.util.Optional;

public interface MessageFilterService {

    /**
     * Returns the first blocklisted phrase found in the given body (case-insensitive,
     * whole word), or empty if the body is clean.
     */
    Optional<String> firstBlockedPhrase(String body);
}
