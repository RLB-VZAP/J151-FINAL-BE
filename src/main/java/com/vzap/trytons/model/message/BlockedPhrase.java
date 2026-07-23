package com.vzap.trytons.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BlockedPhrase {
    private UUID blocklistId;

    private String phrase;

    private UUID createdByUserId;

    private LocalDateTime createdAt;
}
