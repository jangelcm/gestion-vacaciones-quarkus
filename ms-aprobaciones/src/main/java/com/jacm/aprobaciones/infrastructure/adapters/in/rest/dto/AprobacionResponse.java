package com.jacm.aprobaciones.infrastructure.adapters.in.rest.dto;

import com.jacm.aprobaciones.domain.model.Aprobacion;

import java.time.LocalDateTime;

public record AprobacionResponse(
        Long id,
        Long solicitudId,
        String aprobadorId,
        String estado,
        String comentario,
        LocalDateTime fechaAprobacion,
        int nivelAprobacion
) {
    public static AprobacionResponse fromDomain(Aprobacion aprobacion) {
        return new AprobacionResponse(
                aprobacion.getId(),
                aprobacion.getSolicitudId(),
                aprobacion.getAprobadorId(),
                aprobacion.getEstado().name(),
                aprobacion.getComentario(),
                aprobacion.getFechaAprobacion(),
                aprobacion.getNivelAprobacion()
        );
    }
}
