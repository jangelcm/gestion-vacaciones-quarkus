package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import java.time.LocalDate;

public record SolicitudCanceladaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}
