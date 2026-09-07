package com.jacm.aprobaciones.infrastructure.adapters.out.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO del evento 'solicitud.aprobada' publicado hacia Kafka.
 * Los nombres de los campos deben coincidir exactamente con
 * com.vacaciones.politicas.messaging.event.SolicitudAprobadaEvent (ms-politicas),
 * que es quien descuenta el saldo de días al recibir este evento.
 */
public record SolicitudAprobadaEventoDTO(
        String eventoId,
        Long solicitudId,
        Long colaboradorId,
        BigDecimal diasAprobados,
        LocalDateTime fechaAprobacion
) {}
