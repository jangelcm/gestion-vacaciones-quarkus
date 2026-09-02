package com.jacm.solicitudes.api;

import com.jacm.solicitudes.domain.exception.DependenciaNoDisponibleException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DependenciaNoDisponibleExceptionMapper implements ExceptionMapper<DependenciaNoDisponibleException> {

    private record ErrorResponse(String message) {}

    @Override
    public Response toResponse(DependenciaNoDisponibleException exception) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
