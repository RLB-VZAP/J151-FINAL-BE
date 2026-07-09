package com.vzap.trytons.transfer.resource;

import com.vzap.trytons.shared.dto.ErrorResponseDTO;
import com.vzap.trytons.transfer.dto.TransferRequestDTO;
import com.vzap.trytons.transfer.dto.TransferResponseDTO;
import com.vzap.trytons.shared.exceptions.AuthenticationException;
import com.vzap.trytons.shared.exceptions.DataAccessException;
import com.vzap.trytons.shared.exceptions.ResourceNotFoundException;
import com.vzap.trytons.shared.exceptions.ValidationException;
import com.vzap.trytons.transfer.service.TransferService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/transfers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class TransferResource {

    private static final Logger LOGGER = Logger.getLogger(TransferResource.class.getName());

    @Inject
    private TransferService transferService;

    @POST
    public Response executeTransfer(
            @Valid TransferRequestDTO request,
            @Context SecurityContext securityContext){
        try{
            UUID userId = currentUserId(securityContext);
            TransferResponseDTO response = transferService.executeTransfer(userId, request);
            return Response.status(Response.Status.OK).entity(response).build();

        }catch (AuthenticationException e){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }catch (ValidationException e){
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }catch (ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }catch (DataAccessException e){
            return serverError("Failed to execute transfer", e);
        }catch (Exception e){
            return unexpected(e);}
    }

    @GET
    @Path("/{teamId}/history")//This one has a change
    public Response getTransferHistory(
            @PathParam("teamId") UUID teamId,
            @Context SecurityContext securityContext){
        try{
            UUID userId = currentUserId(securityContext);
            List<TransferResponseDTO> history = transferService.getTransfersForTeam(userId, teamId);
            //TransferDAO.getTransferByTeamId(...)

            return Response.status(Response.Status.OK).entity(history).build();

        }catch (AuthenticationException e){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }catch (ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }catch (DataAccessException e){
            return serverError("Failed to retrieve transfer history", e);
        }catch (Exception e){
            return unexpected(e);
        }
    }

    //Helper methods:

    private UUID currentUserId(SecurityContext securityContext){
        Principal principal = securityContext.getUserPrincipal();

        if(principal == null || principal.getName() == null){
            throw new AuthenticationException("Authentication required");
        }

        try{
            return UUID.fromString(principal.getName());
        }catch (IllegalArgumentException e){
            throw new AuthenticationException("Invalid authentication");
        }
    }

    private Response serverError(String message, DataAccessException e){
        LOGGER.log(Level.SEVERE, message, e);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    private Response unexpected(Exception e){
        LOGGER.log(Level.SEVERE, "Unexpected error found in Transfer Resource", e);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of(
                        "An unexpected error has occured", "INTERNAL_SERVER_ERROR"))
                .build();
    }

}
