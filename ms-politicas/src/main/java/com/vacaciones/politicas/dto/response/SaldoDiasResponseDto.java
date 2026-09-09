package com.vacaciones.politicas.dto.response;

public record SaldoDiasResponseDto(
        Long id,
        Long colaboradorId,
        Long politicaId,
        String diasDisponibles,
        String diasUsados,
        String diasAcumulados,
        String diasHabilitados,
        String saldoActual,
        String diasTruncos,
        String diasTrabajados,
        String diasPendientes,
        String createdAt,
        String updatedAt) {
}