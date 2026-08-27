package com.jacm.aprobaciones.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record AprobarSolicitudRequest(
        @NotBlank(message = "El aprobadorId es requerido")
        String aprobadorId,
        String comentario
) {}
