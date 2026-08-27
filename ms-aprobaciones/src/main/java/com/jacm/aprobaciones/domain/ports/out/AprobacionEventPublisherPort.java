package com.jacm.aprobaciones.domain.ports.out;

/**
 * Puerto de salida: publicación de eventos de aprobación hacia Kafka.
 * Implementado en infrastructure/adapters/out/kafka/.
 */
public interface AprobacionEventPublisherPort {

    void publicarSolicitudAprobada(Long solicitudId, String aprobadorId, String comentario);

    void publicarSolicitudRechazada(Long solicitudId, String aprobadorId, String motivo);
}
