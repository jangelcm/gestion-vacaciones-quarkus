package com.jacm.solicitudes.domain.ports.out;

import com.jacm.solicitudes.api.dto.Solicitud;

/**
 * Puerto de salida: publicación de eventos de solicitudes hacia Kafka.
 * Implementado en infrastructure/messaging/producer/SolicitudEventProducer.
 */
public interface SolicitudEventPublisherPort {

    void publicarSolicitudCreada(Solicitud solicitud);
}
