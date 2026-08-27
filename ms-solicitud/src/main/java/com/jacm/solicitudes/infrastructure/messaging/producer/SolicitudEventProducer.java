package com.jacm.solicitudes.infrastructure.messaging.producer;

import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.domain.ports.out.SolicitudEventPublisherPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * Adaptador Kafka: implementa el puerto de salida {@link SolicitudEventPublisherPort}.
 * Publica eventos al topic 'solicitud.creada'.
 */
@ApplicationScoped
public class SolicitudEventProducer implements SolicitudEventPublisherPort {

    private static final Logger LOG = Logger.getLogger(SolicitudEventProducer.class);

    @Inject
    @Channel("solicitud-creada")
    Emitter<Solicitud> solicitudEmitter;

    @Override
    public void publicarSolicitudCreada(Solicitud solicitud) {
        LOG.infof("Publicando evento 'solicitud.creada' para ID: %d", solicitud.getId());
        solicitudEmitter.send(solicitud);
    }
}

