package com.vzap.trytons.dto.message;

import com.vzap.trytons.enums.MessageContactState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** A user the caller could message, with where the two of them stand. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageContactDTO {
    private UUID userId;
    private String username;

    private MessageContactState state;

    /** Set when state is REQUEST_SENT, REQUEST_RECEIVED or DECLINED. */
    private UUID requestId;
}
