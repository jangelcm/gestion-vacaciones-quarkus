package com.jacm.reportes.api.dto;

public record ResumenReporteResponse(
        long pendientes,
        long aprobadas,
        long rechazadas,
        long canceladas,
        long total) {
}
