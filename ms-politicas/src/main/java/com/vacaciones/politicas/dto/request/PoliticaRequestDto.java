package com.vacaciones.politicas.dto.request;

public record PoliticaRequestDto(
        String nombre,
        String tipoVacacion,
        Integer diasBaseAnio,
        Integer antiguedadMinimaMeses,
        Boolean acumulable,
        Integer maxDiasAcumulables,
        Boolean activa) {

    public PoliticaRequestDto {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (tipoVacacion == null || tipoVacacion.isBlank()) {
            throw new IllegalArgumentException("tipoVacacion is required");
        }
        if (diasBaseAnio == null || diasBaseAnio <= 0) {
            throw new IllegalArgumentException("diasBaseAnio must be greater than 0");
        }
        if (antiguedadMinimaMeses == null || antiguedadMinimaMeses < 0) {
            throw new IllegalArgumentException("antiguedadMinimaMeses must be greater than or equal to 0");
        }
        if (acumulable == null) {
            throw new IllegalArgumentException("acumulable is required");
        }
        if (maxDiasAcumulables != null && maxDiasAcumulables <= 0) {
            throw new IllegalArgumentException("maxDiasAcumulables must be greater than 0 when provided");
        }
        if (activa == null) {
            throw new IllegalArgumentException("activa is required");
        }
    }
}