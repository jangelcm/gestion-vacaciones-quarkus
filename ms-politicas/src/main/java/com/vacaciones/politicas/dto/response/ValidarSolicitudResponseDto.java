package com.vacaciones.politicas.dto.response;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ValidarSolicitudResponseDto(
        boolean aprobado,
        long diasSolicitados,
        String motivoRechazo) {
}