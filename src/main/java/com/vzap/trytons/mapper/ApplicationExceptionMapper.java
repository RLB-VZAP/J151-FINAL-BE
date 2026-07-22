package com.vzap.trytons.mapper;

import com.vzap.trytons.dto.shared.ErrorResponseDTO;
import com.vzap.trytons.exceptions.ApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<ApplicationException> {
    private static final Logger LOGGER = Logger.getLogger(ApplicationExceptionMapper.class.getName());

    @Override
    public Response toResponse(ApplicationException e) {
        // Server-side faults (e.g. DataAccessException) are logged centrally so diagnosability is
        // preserved now that resources let application exceptions propagate to this mapper.
        // Expected client-side rejections (4xx) are logged at FINE to avoid log noise.
        if (e.getStatusCode() >= 500) {
            LOGGER.log(Level.SEVERE, "Application error [" + e.getErrorCode() + "]: " + e.getMessage(), e);
        } else {
            LOGGER.log(Level.FINE, () -> "Application rejection [" + e.getErrorCode() + "]: " + e.getMessage());
        }

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(e.getMessage(),e.getErrorCode());
        return Response.status(e.getStatusCode())
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
