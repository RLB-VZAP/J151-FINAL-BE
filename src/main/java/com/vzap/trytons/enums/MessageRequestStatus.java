package com.vzap.trytons.enums;

/**
 * State of a request for permission to exchange direct messages.
 * Distinct from {@link MessageStatus}, which is the moderation state of a
 * league message's content.
 */
public enum MessageRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}
