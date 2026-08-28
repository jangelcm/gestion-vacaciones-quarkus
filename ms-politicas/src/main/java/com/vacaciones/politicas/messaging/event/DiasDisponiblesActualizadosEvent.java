package com.vacaciones.politicas.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiasDisponiblesActualizadosEvent(
        Long colaboradorId,
        BigDecimal diasDisponibles,
        BigDecimal diasUsados,
        String motivoActualizacion,
        LocalDateTime fechaEvento) {
}
