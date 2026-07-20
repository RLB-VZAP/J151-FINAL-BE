package com.vzap.trytons.resource.transfer;

import com.vzap.trytons.annotations.Authenticated;
import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.dto.transfer.TransferRecommendationRequestDTO;
import com.vzap.trytons.dto.transfer.TransferRecommendationResponseDTO;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.AuthorisationException;
import com.vzap.trytons.exceptions.DataAccessException;
import com.vzap.trytons.exceptions.ResourceNotFoundException;
import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.security.AuthPrincipal;
import com.vzap.trytons.service.transfer.TransferRecommendationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Authenticated
@Path("/transfer-recommendations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransferRecommendationResource {

    private static final Logger LOGGER = Logger.getLogger(TransferRecommendationResource.class.getName());

    @Inject
    private TransferRecommendationService transferRecommendationService;

    @POST
    public Response recommendTransfers(@Valid TransferRecommendationRequestDTO request, @Context ContainerRequestContext requestContext) {
        try{
            UUID userId = currentUserId(requestContext);
            TransferRecommendationResponseDTO response = transferRecommendationService.recommendTransfers(userId, request);
            return Response.status(Response.Status.OK).entity(response).build();

        }catch(AuthenticationException e){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }catch (AuthorisationException e){
            return Response.status(Response.Status.FORBIDDEN).build();
        }catch (ResourceNotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).build();
        }catch (DataAccessException e){
            return serverError("Failed to generate transfer recommendations", e);
        }catch (Exception e){
            return unexpected(e);
        }
    }

    private UUID currentUserId(ContainerRequestContext requestContext) {
        Object currentUser = requestContext.getProperty(AuthFilter.CURRENT_USER_PROPERTY);

        if(!(currentUser instanceof AuthPrincipal principal) || principal.getUserId()==null){
            throw new AuthenticationException("Authentication required");
        }
        return  principal.getUserId();
    }

    private Response serverError(String message, DataAccessException e) {
        LOGGER.log(Level.SEVERE, message, e);
        return  Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage())
                .build();
    }

    private Response unexpected(Exception e){
        LOGGER.log(Level.SEVERE, "Unexpected error found in TransferRecommendationResource", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponseDTO.of("An unexpected error has occured", "INTERNAL_SERVER_ERROR"))
                .build();
    }
}
