package com.jacm.aprobaciones.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RechazarSolicitudRequest(
        @NotBlank(message = "El aprobadorId es requerido")
        String aprobadorId,
        @NotBlank(message = "El motivo del rechazo es requerido")
        String motivo
) {}
