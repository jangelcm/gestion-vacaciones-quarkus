package com.jacm.solicitudes.api.dto;

import com.aayushatharva.brotli4j.common.annotations.Local;

import java.time.LocalDate;

public record SolicitudResponse(
    Long id,
    String colaboradorId,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    LocalDate fechaSolicitud,
    String estado
) {
        public static SolicitudResponse fromDomain(Solicitud domain) {
            return new SolicitudResponse(
                    domain.getId(),
                    domain.getColaboradorId(),
                    domain.getFechaInicio(),
                    domain.getFechaFin(),
                    domain.getFechaSolicitud(),
                    domain.getEstado().name()
            );
        }
 }
