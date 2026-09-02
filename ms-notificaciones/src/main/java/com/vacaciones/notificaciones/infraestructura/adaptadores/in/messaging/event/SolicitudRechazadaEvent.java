package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import java.time.LocalDate;

public record SolicitudRechazadaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        String colaboradorEmail,
        String colaboradorNombre,
        String motivoRechazo,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}
