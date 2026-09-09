package com.jacm.consultas.messaging.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiasDisponiblesActualizadosEvent(
        Long colaboradorId,
        Long politicaId,
        LocalDate fechaInicioPolitica,
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
