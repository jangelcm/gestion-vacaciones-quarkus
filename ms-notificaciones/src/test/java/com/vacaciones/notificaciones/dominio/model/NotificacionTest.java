package com.vacaciones.notificaciones.dominio.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificacionTest {

    private static final Destinatario DESTINATARIO =
            new Destinatario(1001L, "colaborador@empresa.com", "Ana Perez");

    @Test
    void shouldReconstructNotificacionWithExactGivenState() {
        LocalDateTime fechaCreacion = LocalDateTime.of(2026, 8, 29, 9, 0, 0);
        LocalDateTime fechaEnvio = LocalDateTime.of(2026, 8, 29, 9, 5, 0);

        Notificacion notificacion = new Notificacion(
                "id-mongo-1",
                "evt-1",
                TipoNotificacion.EMAIL,
                DESTINATARIO,
                "asunto",
                "cuerpo",
                EstadoNotificacion.ENVIADO,
                "solicitud.aprobada",
                fechaCreacion,
                fechaEnvio);

        assertEquals("id-mongo-1", notificacion.getId());
        assertEquals(EstadoNotificacion.ENVIADO, notificacion.getEstado());
        assertEquals(fechaCreacion, notificacion.getFechaCreacion());
        assertEquals(fechaEnvio, notificacion.getFechaEnvio());
    }

    @Test
    void shouldCreateNotificacionWithPendienteStateAndNullFechaEnvio() {
        Notificacion notificacion = assertDoesNotThrow(() -> new Notificacion(
                "evt-1",
                TipoNotificacion.EMAIL,
                DESTINATARIO,
                "Solicitud aprobada",
                "Tu solicitud de vacaciones fue aprobada",
                "solicitud.aprobada"));

        assertEquals(EstadoNotificacion.PENDIENTE, notificacion.getEstado());
        assertNull(notificacion.getFechaEnvio());
        assertNotNull(notificacion.getFechaCreacion());
        assertNull(notificacion.getId());
        assertEquals("evt-1", notificacion.getEventoId());
        assertEquals(TipoNotificacion.EMAIL, notificacion.getTipo());
        assertEquals(DESTINATARIO, notificacion.getDestinatario());
        assertEquals("Solicitud aprobada", notificacion.getAsunto());
        assertEquals("solicitud.aprobada", notificacion.getEventoOrigen());
    }

    @Test
    void shouldThrowWhenAsuntoIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, null, "cuerpo", "solicitud.aprobada"));
    }

    @Test
    void shouldThrowWhenAsuntoIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "", "cuerpo", "solicitud.aprobada"));
    }

    @Test
    void shouldThrowWhenCuerpoIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", null, "solicitud.aprobada"));
    }

    @Test
    void shouldThrowWhenCuerpoIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "", "solicitud.aprobada"));
    }

    @Test
    void shouldMarkAsEnviadaSuccessfullyFromPendiente() {
        Notificacion notificacion = new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");

        notificacion.marcarComoEnviada();

        assertEquals(EstadoNotificacion.ENVIADO, notificacion.getEstado());
        assertNotNull(notificacion.getFechaEnvio());
    }

    @Test
    void shouldThrowWhenMarkingAsEnviadaTwice() {
        Notificacion notificacion = new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");
        notificacion.marcarComoEnviada();

        assertThrows(EstadoNotificacionInvalidoException.class, notificacion::marcarComoEnviada);
    }

    @Test
    void shouldMarkAsFallidaSuccessfullyFromPendiente() {
        Notificacion notificacion = new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");

        notificacion.marcarComoFallida();

        assertEquals(EstadoNotificacion.FALLIDO, notificacion.getEstado());
    }

    @Test
    void shouldThrowWhenMarkingAsFallidaAfterEnviada() {
        Notificacion notificacion = new Notificacion(
                "evt-1", TipoNotificacion.EMAIL, DESTINATARIO, "asunto", "cuerpo", "solicitud.aprobada");
        notificacion.marcarComoEnviada();

        assertThrows(EstadoNotificacionInvalidoException.class, notificacion::marcarComoFallida);
    }
}
