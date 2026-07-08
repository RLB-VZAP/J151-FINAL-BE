package com.vzap.trytons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinLeagueResponseDTO {
    private UUID leagueId;
    private String leagueName;
    private String message;
    private UUID membershipId;
}
