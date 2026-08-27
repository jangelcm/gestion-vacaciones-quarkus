package com.jacm.aprobaciones.infrastructure.adapters.out.kafka.dto;

/**
 * DTO del evento publicado hacia Kafka cuando una solicitud es aprobada o rechazada.
 * Consumido por ms-solicitudes y ms-consultas.
 */
public record AprobacionEventoDTO(
        Long solicitudId,
        String aprobadorId,
        String estado,
        String comentario
) {}
