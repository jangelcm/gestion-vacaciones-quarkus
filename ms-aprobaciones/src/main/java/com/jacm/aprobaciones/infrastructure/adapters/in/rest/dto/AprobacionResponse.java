package com.jacm.aprobaciones.infrastructure.adapters.in.rest.dto;

import com.jacm.aprobaciones.domain.model.Aprobacion;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDateTime;

@RegisterForReflection
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
