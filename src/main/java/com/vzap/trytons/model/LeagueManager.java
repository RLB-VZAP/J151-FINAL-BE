package com.vzap.trytons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueManager extends RegisteredUser{
private Date managerSince;
private UUID userID;

}
