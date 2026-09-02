package com.jacm.consultas.api;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.NoSuchElementException;

@Provider
public class NoSuchElementExceptionMapper implements ExceptionMapper<NoSuchElementException> {

    private record ErrorResponse(String message) {}

    @Override
    public Response toResponse(NoSuchElementException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
