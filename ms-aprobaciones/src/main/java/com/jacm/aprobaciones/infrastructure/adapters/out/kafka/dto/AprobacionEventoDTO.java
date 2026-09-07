package com.jacm.aprobaciones.infrastructure.adapters.out.kafka.dto;

import java.time.LocalDate;

/**
 * DTO del evento 'solicitud.rechazada' publicado hacia Kafka.
 * Tiene dos consumidores con necesidades distintas sobre el MISMO topic:
 * - ms-solicitud (copia local de este record con solo solicitudId/aprobadorId/estado/comentario):
 *   actualiza el estado local de la solicitud. Ignora colaboradorId/fechaInicio/fechaFin (no los
 *   tiene en su record, Jackson los ignora silenciosamente).
 * - ms-notificaciones (SolicitudRechazadaEvent): usa solicitudId/colaboradorId/fechaInicio/
 *   fechaFin/comentario para notificar al colaborador. Ignora aprobadorId/estado.
 */
public record AprobacionEventoDTO(
        Long solicitudId,
        String aprobadorId,
        String estado,
        String comentario,
        Long colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}
