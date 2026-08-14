package com.jacm.solicitudes.api;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.NoSuchElementException;

@Provider
public class NoSuchElementExceptionMapper implements ExceptionMapper<NoSuchElementException> {

    private static record ErrorResponse(String message) {}

    @Override
    public Response toResponse(NoSuchElementException e) {
        var errorResponse = new ErrorResponse(e.getMessage());
        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
    }
}
