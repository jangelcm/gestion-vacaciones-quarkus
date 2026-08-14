package com.jacm.solicitudes.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitudCrearRequest(
        @NotBlank(message = "El id del colaborador es requerido")
        String colaboradorId,

        @NotNull(message = "La fecha de inicio es requerida")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es requerida")
        LocalDate fechaFin
) {}