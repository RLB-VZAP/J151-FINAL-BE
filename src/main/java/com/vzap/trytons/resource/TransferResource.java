package com.vzap.trytons.resource;

import com.vzap.trytons.Annotations.Authenticated;
import com.vzap.trytons.dto.ApiResponseDTO;
import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.dto.TransferRequestDTO;
import com.vzap.trytons.dto.TransferResponseDTO;
import com.vzap.trytons.exceptions.ApplicationException;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.TransferService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Authenticated
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
            @Context ContainerRequestContext requestContext) {
        try {
            String actorUserId = currentUserId(requestContext);

            TransferResponseDTO response = transferService.executeTransfer(actorUserId, request);

            ApiResponseDTO<TransferResponseDTO> payload =
                    ApiResponseDTO.success("Transfer executed successfully.", response);

            return Response.ok(payload).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    @GET
    @Path("/{teamId}/history")
    public Response listTransferHistory(
            @PathParam("teamId") String teamId,
            @Context ContainerRequestContext requestContext) {
        try {
            String actorUserId = currentUserId(requestContext);

            List<TransferResponseDTO> history =
                    transferService.listTransferHistory(actorUserId, teamId);

            ApiResponseDTO<List<TransferResponseDTO>> payload =
                    ApiResponseDTO.success("Transfer history retrieved successfully.", history);

            return Response.ok(payload).build();

        } catch (ApplicationException e) {
            return handledApplicationError(e);

        } catch (Exception e) {
            return unexpected(e);
        }
    }

    private String currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if (!(currentUser instanceof AuthPrincipal principal) || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }

        return principal.getUserId().toString();
    }

    private Response handledApplicationError(ApplicationException e) {
        ErrorResponseDTO errorPayload = ErrorResponseDTO.of(e.getMessage(), e.getErrorCode());

        return Response.status(e.getStatusCode())
                .entity(errorPayload)
                .build();
    }

    private Response unexpected(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error in TransferResource.", e);

        ErrorResponseDTO errorPayload =
                ErrorResponseDTO.of("An unexpected error occurred.", "INTERNAL_SERVER_ERROR");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorPayload)
                .build();
    }
}