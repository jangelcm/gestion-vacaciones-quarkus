package com.vacaciones.politicas.dto.request;

import java.time.LocalDate;

public record ValidarSolicitudRequestDto(
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String tipoVacacion,
        Integer antiguedadMeses) {

    public ValidarSolicitudRequestDto {
        if (colaboradorId == null || colaboradorId <= 0) {
            throw new IllegalArgumentException("colaboradorId must be greater than 0");
        }
        if (fechaInicio == null) {
            throw new IllegalArgumentException("fechaInicio is required");
        }
        if (fechaFin == null) {
            throw new IllegalArgumentException("fechaFin is required");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("fechaFin cannot be before fechaInicio");
        }
        if (tipoVacacion != null && tipoVacacion.isBlank()) {
            throw new IllegalArgumentException("tipoVacacion cannot be blank");
        }
        if (antiguedadMeses != null && antiguedadMeses < 0) {
            throw new IllegalArgumentException("antiguedadMeses must be greater than or equal to 0");
        }
    }
}