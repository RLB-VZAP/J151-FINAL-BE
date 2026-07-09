package com.vzap.trytons.social.model;

import com.vzap.trytons.social.enums.ReportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Report {

    private UUID reportId;
    private String reportReason;
    private LocalDateTime reportDate;
    private ReportStatus status;
    private Boolean resolved;
    private String resolution;

    private ChatMessage chatMessage;

    private User reporter;
    private User reportedUser;
    private Administrator resolvedByAdmin;
}