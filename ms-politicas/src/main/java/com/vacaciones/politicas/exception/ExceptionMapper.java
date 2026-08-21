package com.vacaciones.politicas.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Provider
public class ExceptionMapper implements jakarta.ws.rs.ext.ExceptionMapper<Throwable> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        Response.Status status = resolveStatus(exception);
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now().format(FORMATTER),
                exception.getMessage(),
                resolveUrl(),
                String.valueOf(status.getStatusCode()));

        return Response.status(status)
                .entity(errorResponse)
                .build();
    }

    void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    private Response.Status resolveStatus(Throwable exception) {
        if (exception instanceof RuntimeCustomException runtimeCustomException) {
            return runtimeCustomException.getStatus();
        }
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    private String resolveUrl() {
        if (uriInfo == null || uriInfo.getRequestUri() == null) {
            return "";
        }
        return uriInfo.getRequestUri().getPath();
    }
}