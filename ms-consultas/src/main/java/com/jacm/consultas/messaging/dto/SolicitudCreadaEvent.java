package com.jacm.consultas.messaging.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;

@RegisterForReflection
public record SolicitudCreadaEvent(
        Long id,
        String colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaSolicitud,
        String estado) {
}
