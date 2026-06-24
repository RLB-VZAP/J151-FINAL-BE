package com.vzap.trytons.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class ChatMessage {
    private UUID messageId;
    private String content;
    private LocalDateTime sentDate;
    private Boolean removed;

    private League league;
    private List<Report> reports = new ArrayList<>();
}
