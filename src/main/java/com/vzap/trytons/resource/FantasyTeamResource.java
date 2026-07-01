package com.vzap.trytons.resource;

import com.vzap.trytons.dto.ErrorResponse;
import com.vzap.trytons.dto.FantasyTeamPlayerSelectionRequestDTO;
import com.vzap.trytons.dto.FantasyTeamRequestDTO;
import com.vzap.trytons.dto.FantasyTeamResponseDTO;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.service.FantasyTeamService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationPath("/api")
@Path("/fantasy-team")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FantasyTeamResource {
    private static final Logger LOGGER = Logger.getLogger(UserResources.class.getName());
    @Inject
    private FantasyTeamService fantasyTeamService;

    private Response serverError(String message , DataAccessException e) {
      LOGGER.log(Level.SEVERE, message, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in PositionResource.", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorResponse.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR")).build();
    }
}
