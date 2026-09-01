package com.vacaciones.notificaciones.infraestructura.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.vacaciones.notificaciones.dominio.model.EstadoNotificacionInvalidoException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class ExceptionMapperTest {

    private final ExceptionMapper mapper = new ExceptionMapper();

    @Test
    void shouldMapNotificacionNoEncontradaExceptionTo404() {
        Response response = mapper.toResponse(new NotificacionNoEncontradaException("no existe"));

        assertEquals(404, response.getStatus());
        assertEquals("no existe", ((ErrorResponse) response.getEntity()).mensaje());
    }

    @Test
    void shouldMapEstadoNotificacionInvalidoExceptionTo409() {
        Response response = mapper.toResponse(new EstadoNotificacionInvalidoException("estado invalido"));

        assertEquals(409, response.getStatus());
    }

    @Test
    void shouldMapUnknownExceptionTo500() {
        Response response = mapper.toResponse(new RuntimeException("fallo inesperado"));

        assertEquals(500, response.getStatus());
    }
}
