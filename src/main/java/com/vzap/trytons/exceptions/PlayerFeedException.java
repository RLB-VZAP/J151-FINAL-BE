package com.vzap.trytons.exceptions;

/**
 * Raised when the external live-player feed cannot be reached or returns an
 * unusable response. Mapped to HTTP 502 (Bad Gateway): the fault is upstream,
 * not with the caller's request.
 */
public class PlayerFeedException extends ApplicationException {
    public PlayerFeedException(String message) {
        super(message, 502, "PLAYER_FEED_ERROR");
    }

    public PlayerFeedException(String message, Throwable cause) {
        super(message, 502, "PLAYER_FEED_ERROR", cause);
    }
}
