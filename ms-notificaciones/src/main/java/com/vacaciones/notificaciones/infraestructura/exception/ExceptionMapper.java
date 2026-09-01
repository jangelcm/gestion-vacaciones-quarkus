package com.vacaciones.notificaciones.infraestructura.exception;

import com.vacaciones.notificaciones.dominio.model.EstadoNotificacionInvalidoException;
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

    private Response.Status resolveStatus(Throwable exception) {
        if (exception instanceof NotificacionNoEncontradaException) {
            return Response.Status.NOT_FOUND;
        }
        if (exception instanceof EstadoNotificacionInvalidoException) {
            return Response.Status.CONFLICT;
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
