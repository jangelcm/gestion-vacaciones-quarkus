package com.vacaciones.politicas.messaging.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RegisterForReflection
public record SolicitudAprobadaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        BigDecimal diasAprobados,
        LocalDateTime fechaAprobacion) {
}
