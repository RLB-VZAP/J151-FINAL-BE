package com.vzap.trytons.service.notification;

import java.util.Collection;
import java.util.Map;

public interface FcmService {

    /**
     * Sends a push notification to the given device tokens. Never throws:
     * failures (including FCM being disabled) are logged and swallowed so that
     * message persistence is never affected.
     */
    void send(Collection<String> tokens, String title, String body, Map<String, String> data);
}
