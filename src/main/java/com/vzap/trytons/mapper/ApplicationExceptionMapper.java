package com.vzap.trytons.mapper;

import com.vzap.trytons.dto.ErrorResponseDTO;
import com.vzap.trytons.exceptions.ApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<ApplicationException> {
    @Override
    public Response toResponse(ApplicationException e) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(e.getMessage(),e.getErrorCode());
        return Response.status(e.getStatusCode())
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
