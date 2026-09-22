package com.jacm.reportes.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ResumenReporteResponse(
        long pendientes,
        long aprobadas,
        long rechazadas,
        long canceladas,
        long total) {
}
