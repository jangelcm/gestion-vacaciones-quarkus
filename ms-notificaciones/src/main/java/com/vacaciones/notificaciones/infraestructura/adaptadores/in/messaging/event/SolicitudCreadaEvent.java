package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import java.time.LocalDate;

public record SolicitudCreadaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        String colaboradorEmail,
        String colaboradorNombre,
        String aprobadorEmail,
        String aprobadorNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}
