package com.jacm.solicitudes.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;

@RegisterForReflection
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
