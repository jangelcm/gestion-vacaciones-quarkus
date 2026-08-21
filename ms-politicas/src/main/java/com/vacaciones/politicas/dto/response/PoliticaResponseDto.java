package com.vacaciones.politicas.dto.response;

public record PoliticaResponseDto(
        Long id,
        String nombre,
        String tipoVacacion,
        Integer diasBaseAnio,
        Integer antiguedadMinimaMeses,
        Boolean acumulable,
        Integer maxDiasAcumulables,
        Boolean activa,
        String createdAt,
        String updatedAt) {
}