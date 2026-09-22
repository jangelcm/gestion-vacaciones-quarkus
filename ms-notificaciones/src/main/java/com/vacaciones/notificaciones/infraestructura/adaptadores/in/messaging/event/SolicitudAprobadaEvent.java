package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.math.BigDecimal;
import java.time.LocalDate;

@RegisterForReflection
public record SolicitudAprobadaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal diasAprobados,
        String aprobadorId) {
}
