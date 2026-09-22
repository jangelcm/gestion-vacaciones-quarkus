package com.jacm.consultas.messaging.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RegisterForReflection
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
        BigDecimal diasAcumulados,
        LocalDate fechaIngresoColaborador,
        String motivoActualizacion,
        LocalDateTime fechaEvento) {
}
