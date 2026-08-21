package com.vacaciones.politicas.dto.response;

public record ValidarSolicitudResponseDto(
        boolean aprobado,
        long diasSolicitados,
        String motivoRechazo) {
}