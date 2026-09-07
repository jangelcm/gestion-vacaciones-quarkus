package com.jacm.aprobaciones.infrastructure.adapters.out.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO del evento 'solicitud.aprobada' publicado hacia Kafka.
 * Tiene dos consumidores con necesidades distintas sobre el MISMO topic:
 * - ms-politicas (SolicitudAprobadaEvent): usa eventoId/solicitudId/colaboradorId/diasAprobados
 *   para descontar el saldo. Ignora fechaInicio/fechaFin (no los tiene en su record).
 * - ms-notificaciones (SolicitudAprobadaEvent): usa eventoId/solicitudId/colaboradorId/
 *   fechaInicio/fechaFin para armar el mensaje. Ignora diasAprobados/fechaAprobacion.
 * Por eso el evento lleva TODOS los campos que cualquiera de los dos necesita.
 */
public record SolicitudAprobadaEventoDTO(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        BigDecimal diasAprobados,
        LocalDateTime fechaAprobacion,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}
