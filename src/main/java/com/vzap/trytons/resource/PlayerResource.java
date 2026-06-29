package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponse;
import com.vzap.trytons.dto.PlayerRequestDTO;
import com.vzap.trytons.dto.PlayerResponseDTO;
import com.vzap.trytons.exceptions.ConflictException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.model.Player;
import com.vzap.trytons.service.PlayerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationPath("/api")
@Path("/player")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerResource {
    private static final Logger LOGGER = Logger.getLogger(UserResources.class.getName());
    @Inject
    private PlayerService playerService;
    @GET
    public Response listPlayers(@QueryParam("search")  String search, @QueryParam("clubId") UUID clubId, @QueryParam("PositionId" ) UUID positionId) {
        List<Player> players = playerService.search(search, clubId, positionId);
        List<PlayerResponseDTO> body = players.stream().map(this::toResponse).toList();
        return Response.ok(body).build();
    }

    private PlayerResponseDTO toResponse(Player player) {
        return new PlayerResponseDTO(
                player.getPlayerId(),
                player.getPlayerName(),
                player.getValue(),
                player.getTotalFantasyPoints(),
                player.getisActive(),
                player.getClub() != null ? Integer.parseInt(player.getClub().getClubName()) : null,
                player.getPosition() != null ? Integer.parseInt(player.getPosition().getPositionName()) : null
        );
    }
}
