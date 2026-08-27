package com.jacm.aprobaciones.infrastructure.adapters.in.kafka.dto;

import java.time.LocalDate;

/**
 * DTO para deserializar el evento 'solicitud.creada' producido por ms-solicitudes.
 * Debe reflejar la estructura del objeto Solicitud serializado por ms-solicitudes.
 */
public record SolicitudCreadaEventDTO(
        Long id,
        String colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaSolicitud,
        String estado
) {}
