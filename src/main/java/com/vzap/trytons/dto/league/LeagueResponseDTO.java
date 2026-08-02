package com.vzap.trytons.dto.league;

import com.vzap.trytons.enums.LeagueStatus;
import com.vzap.trytons.enums.LeagueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueResponseDTO {
    private UUID leagueId;
    private UUID managerUserId;

    /**
     * The manager's username, or null for a public league — those are run by
     * the administrators and have no manager. The frontend has always declared
     * this field; nothing ever populated it, so every league rendered as
     * "Managed by the league".
     */
    private String managerDisplayName;

    private String leagueName;
    private String description;

    private LeagueType leagueType;

    private LocalDateTime creationDate;

    private Boolean isActive;

    private int maxMembers;

    private String leagueCode;

    // League.status/startedAt existed on the model and in the database but never
    // reached the client, so every league looked FORMING to the UI -- "start league"
    // showed on leagues already running and the tournament link never appeared.
    private LeagueStatus status;

    private LocalDateTime startedAt;
}
