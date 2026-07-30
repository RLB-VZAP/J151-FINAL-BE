package com.vzap.trytons.enums;

/**
 * The caller's messaging relationship with another user, as the directory
 * presents it. Derived from message_request and user_block rather than stored:
 * it collapses "whose turn is it" into one value the UI can switch on, which a
 * bare {@link MessageRequestStatus} cannot express (PENDING means "waiting on
 * them" or "waiting on you" depending on which side the caller is).
 */
public enum MessageContactState {
    /** No request either way — the caller may send one. */
    NONE,
    /** The caller has asked and is waiting for a reply. */
    REQUEST_SENT,
    /** The other user has asked the caller, who can accept or decline. */
    REQUEST_RECEIVED,
    /** Accepted in either direction — direct messages are open. */
    ACCEPTED,
    /** The caller's request was declined; they may ask again. */
    DECLINED,
    /** A block exists in either direction; messaging is off the table. */
    BLOCKED
}
