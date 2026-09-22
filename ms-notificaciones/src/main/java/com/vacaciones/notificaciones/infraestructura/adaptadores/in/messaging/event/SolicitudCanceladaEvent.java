package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;

@RegisterForReflection
public record SolicitudCanceladaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}
