package com.jacm.reportes.api.dto;

import java.math.BigDecimal;

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
