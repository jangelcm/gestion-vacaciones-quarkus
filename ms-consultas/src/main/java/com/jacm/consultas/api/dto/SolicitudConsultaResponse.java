package com.jacm.consultas.api.dto;

import com.jacm.consultas.model.SolicitudReadDocument;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RegisterForReflection
public record SolicitudConsultaResponse(
        Long id,
        String colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaSolicitud,
        String estado,
        LocalDateTime ultimaActualizacion) {

    public static SolicitudConsultaResponse fromDocument(SolicitudReadDocument doc) {
        return new SolicitudConsultaResponse(
                doc.solicitudId,
                doc.colaboradorId,
                doc.fechaInicio,
                doc.fechaFin,
                doc.fechaSolicitud,
                doc.estado,
                doc.ultimaActualizacion);
    }
}
