package com.vzap.trytons.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Body of "ask this user for permission to message them". */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMessageRequestRequestDTO {
    private UUID addresseeUserId;

    /** Optional note shown to the addressee alongside the request. */
    private String introMessage;
}
