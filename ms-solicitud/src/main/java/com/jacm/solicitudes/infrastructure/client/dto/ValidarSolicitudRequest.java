package com.jacm.solicitudes.infrastructure.client.dto;

import java.time.LocalDate;

public record ValidarSolicitudRequest(
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String tipoVacacion,
        Integer antiguedadMeses) {
}
