package com.vacaciones.politicas.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiasDisponiblesActualizadosEvent(
        Long colaboradorId,
        BigDecimal diasDisponibles,
        BigDecimal diasUsados,
        BigDecimal diasHabilitados,
        BigDecimal saldoActual,
        BigDecimal diasTruncos,
        Integer diasTrabajados,
        BigDecimal diasPendientes,
        String motivoActualizacion,
        LocalDateTime fechaEvento) {
}
