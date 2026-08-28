package com.vacaciones.politicas.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitudAprobadaEvent(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        BigDecimal diasAprobados,
        LocalDateTime fechaAprobacion) {
}
