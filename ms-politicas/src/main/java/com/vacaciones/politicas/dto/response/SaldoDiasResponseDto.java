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
        String fechaIngresoColaborador,
        String fechaAsignacionPolitica,
        String createdAt,
        String updatedAt) {

        public SaldoDiasResponseDto(
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
                this(
                                id,
                                colaboradorId,
                                politicaId,
                                diasDisponibles,
                                diasUsados,
                                diasAcumulados,
                                diasHabilitados,
                                saldoActual,
                                diasTruncos,
                                diasTrabajados,
                                diasPendientes,
                                null,
                                null,
                                createdAt,
                                updatedAt);
        }
}