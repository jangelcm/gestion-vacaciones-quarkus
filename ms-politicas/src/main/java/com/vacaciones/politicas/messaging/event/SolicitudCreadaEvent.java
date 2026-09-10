package com.vacaciones.politicas.messaging.event;

import java.time.LocalDate;

public record SolicitudCreadaEvent(
        Long id,
        String colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaSolicitud,
        String estado) {
}
