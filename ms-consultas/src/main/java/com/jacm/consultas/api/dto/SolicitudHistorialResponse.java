package com.jacm.consultas.api.dto;

import com.jacm.consultas.model.SolicitudHistorialDocument;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDateTime;

@RegisterForReflection
public record SolicitudHistorialResponse(
        Long solicitudId,
        String estado,
        String detalle,
        LocalDateTime fechaEvento) {

    public static SolicitudHistorialResponse fromDocument(SolicitudHistorialDocument doc) {
        return new SolicitudHistorialResponse(doc.solicitudId, doc.estado, doc.detalle, doc.fechaEvento);
    }
}
