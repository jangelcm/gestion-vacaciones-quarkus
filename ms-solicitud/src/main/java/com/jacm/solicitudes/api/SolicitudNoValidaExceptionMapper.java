package com.jacm.solicitudes.api;

import com.jacm.solicitudes.domain.exception.SolicitudNoValidaException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SolicitudNoValidaExceptionMapper implements ExceptionMapper<SolicitudNoValidaException> {

    private record ErrorResponse(String message) {}

    @Override
    public Response toResponse(SolicitudNoValidaException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
