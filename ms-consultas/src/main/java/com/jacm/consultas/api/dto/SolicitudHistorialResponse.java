package com.jacm.consultas.api.dto;

import com.jacm.consultas.model.SolicitudHistorialDocument;
import java.time.LocalDateTime;

public record SolicitudHistorialResponse(
        Long solicitudId,
        String estado,
        String detalle,
        LocalDateTime fechaEvento) {

    public static SolicitudHistorialResponse fromDocument(SolicitudHistorialDocument doc) {
        return new SolicitudHistorialResponse(doc.solicitudId, doc.estado, doc.detalle, doc.fechaEvento);
    }
}
