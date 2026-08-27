package com.jacm.aprobaciones.infrastructure.config;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<NoSuchElementException> {

    private record ErrorResponse(String mensaje, String timestamp) {}

    @Override
    public Response toResponse(NoSuchElementException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage(), LocalDateTime.now().toString()))
                .build();
    }
}
