package com.jacm.solicitudes.infrastructure.messaging.consumer;

import com.jacm.solicitudes.domain.model.EstadoSolicitud;
import com.jacm.solicitudes.domain.service.SolicitudService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;


// Record auxiliar para recibir la respuesta del MCSV Aprobaciones
record SolicitudProcesadaDTO(Long solicitudId, String estadoFinal) {}

@ApplicationScoped
public class SolicitudProcesadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudProcesadaConsumer.class);

    @Inject
    SolicitudService service;

    @Incoming("solicitud-procesada")
    private void consumirSolicitudProcesada(SolicitudProcesadaDTO evento) {
        LOG.infof("Evento 'solicitud-procesada' recibido. ID Solicitud: %d, Estado: %s",
                evento.solicitudId(), evento.estadoFinal());

        try {
            EstadoSolicitud nuevoEstado = EstadoSolicitud.valueOf(evento.estadoFinal());
            service.actualizarEstado(evento.solicitudId(), nuevoEstado);
            LOG.info("Estado de solicitud actualizado en DB local exitosamente.");
        } catch (IllegalArgumentException e) {
            LOG.errorf("Estado recibido no válido: %s", evento.estadoFinal());
        }
    }

}
