package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitudAprobadaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal diasAprobados,
        String aprobadorId) {
}
