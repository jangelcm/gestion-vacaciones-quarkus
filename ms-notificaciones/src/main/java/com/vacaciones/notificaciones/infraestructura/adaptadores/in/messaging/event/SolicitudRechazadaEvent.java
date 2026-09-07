package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import java.time.LocalDate;

/**
 * ms-aprobaciones publica AprobacionEventoDTO (solicitudId/aprobadorId/estado/comentario/
 * colaboradorId/fechaInicio/fechaFin) al topic 'solicitud.rechazada' — no trae "eventoId" ni
 * "motivoRechazo" (el campo real se llama "comentario"). aprobadorId/estado se ignoran aqui.
 */
public record SolicitudRechazadaEvent(
        Long solicitudId,
        String comentario,
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}
