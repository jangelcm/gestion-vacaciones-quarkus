package com.jacm.solicitudes.infrastructure.client.dto;

public record ValidarSolicitudResponse(
        boolean aprobado,
        long diasSolicitados,
        String motivoRechazo) {
}
