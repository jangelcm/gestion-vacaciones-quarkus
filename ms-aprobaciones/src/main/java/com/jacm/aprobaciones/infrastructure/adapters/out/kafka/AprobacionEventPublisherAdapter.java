package com.jacm.aprobaciones.infrastructure.adapters.out.kafka;

import com.jacm.aprobaciones.domain.ports.out.AprobacionEventPublisherPort;
import com.jacm.aprobaciones.infrastructure.adapters.out.kafka.dto.AprobacionEventoDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * Adaptador Kafka: implementa el puerto de salida {@link AprobacionEventPublisherPort}.
 * Publica eventos hacia los topics 'solicitud.aprobada' y 'solicitud.rechazada'.
 */
@ApplicationScoped
public class AprobacionEventPublisherAdapter implements AprobacionEventPublisherPort {

    private static final Logger LOG = Logger.getLogger(AprobacionEventPublisherAdapter.class);

    @Inject
    @Channel("solicitud-aprobada")
    Emitter<AprobacionEventoDTO> aprobadaEmitter;

    @Inject
    @Channel("solicitud-rechazada")
    Emitter<AprobacionEventoDTO> rechazadaEmitter;

    @Override
    public void publicarSolicitudAprobada(Long solicitudId, String aprobadorId, String comentario) {
        var evento = new AprobacionEventoDTO(solicitudId, aprobadorId, "APROBADO", comentario);
        LOG.infof("Publicando evento 'solicitud.aprobada' para solicitud ID: %d", solicitudId);
        aprobadaEmitter.send(evento);
    }

    @Override
    public void publicarSolicitudRechazada(Long solicitudId, String aprobadorId, String motivo) {
        var evento = new AprobacionEventoDTO(solicitudId, aprobadorId, "RECHAZADO", motivo);
        LOG.infof("Publicando evento 'solicitud.rechazada' para solicitud ID: %d", solicitudId);
        rechazadaEmitter.send(evento);
    }
}
