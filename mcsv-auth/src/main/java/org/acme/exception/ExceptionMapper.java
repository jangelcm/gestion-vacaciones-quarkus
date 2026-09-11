package org.acme.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

/**
 * Atrapa toda excepcion no controlada para que el front siempre reciba un cuerpo JSON
 * consistente ({hora, mensaje, url, codeStatus}) en vez del formato crudo por defecto de
 * Quarkus. Ademas de RuntimeCustomException (con su propio status), reconoce las excepciones
 * idiomaticas de Java que ya se usan en el codigo existente para no tener que reescribir cada
 * punto donde se lanzan.
 */
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
        if (exception instanceof RuntimeCustomException runtimeCustomException) {
            return runtimeCustomException.getStatus();
        }
        if (exception instanceof NoSuchElementException) {
            return Response.Status.NOT_FOUND;
        }
        if (exception instanceof IllegalStateException) {
            return Response.Status.CONFLICT;
        }
        if (exception instanceof IllegalArgumentException) {
            return Response.Status.BAD_REQUEST;
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
