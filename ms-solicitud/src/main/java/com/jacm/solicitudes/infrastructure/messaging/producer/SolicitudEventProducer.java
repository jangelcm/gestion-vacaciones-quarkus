package com.jacm.solicitudes.infrastructure.messaging.producer;

import com.jacm.solicitudes.api.dto.Solicitud;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class SolicitudEventProducer {

    @Inject
    @Channel("solicitud-creada") // Mapeado en application.properties
    Emitter<Solicitud> solicitudEmitter;

    public void enviarSolicitudCreada(Solicitud solicitud) {
        LOG.infof("Publicando evento 'solicitud-creada' para ID: %d", solicitud.getId());
        solicitudEmitter.send(solicitud);
    }
}
