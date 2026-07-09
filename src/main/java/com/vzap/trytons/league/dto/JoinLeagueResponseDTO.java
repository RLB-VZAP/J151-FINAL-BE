package com.vzap.trytons.league.dto;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinLeagueResponseDTO {
    private UUID leagueId;
    private String leagueName;
    private String message;
    private UUID membershipId;
}
