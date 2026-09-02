package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import java.time.LocalDate;

public record SolicitudAprobadaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        String colaboradorEmail,
        String colaboradorNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}
