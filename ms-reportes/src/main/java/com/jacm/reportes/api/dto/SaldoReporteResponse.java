package com.jacm.reportes.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.math.BigDecimal;

@RegisterForReflection
public record SaldoReporteResponse(
        Long colaboradorId,
        Long politicaId,
        String politicaNombre,
        BigDecimal diasDisponibles,
        BigDecimal diasGozados,
        BigDecimal diasHabilitados,
        BigDecimal saldoActual,
        BigDecimal diasAcumulados,
        BigDecimal diasPendientes) {
}
