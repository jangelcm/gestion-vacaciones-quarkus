package com.jacm.solicitudes.infrastructure.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacm.solicitudes.domain.model.EstadoSolicitud;
import com.jacm.solicitudes.domain.ports.in.SolicitudUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * DTO del evento publicado por ms-aprobaciones al aprobar o rechazar una solicitud.
 */
record AprobacionEventoDTO(Long solicitudId, String aprobadorId, String estado, String comentario) {}

/**
 * Consumidor de eventos de aprobación/rechazo producidos por ms-aprobaciones.
 * Actualiza el estado local de la solicitud en la base de datos de ms-solicitudes.
 */
@ApplicationScoped
public class SolicitudProcesadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudProcesadaConsumer.class);

    @Inject
    SolicitudUseCase solicitudUseCase;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Consume el evento 'solicitud.aprobada' producido por ms-aprobaciones.
     * Canal: solicitud-aprobada → topic: solicitud.aprobada
     */
    @Incoming("solicitud-aprobada")
    public void onSolicitudAprobada(String mensaje) {
        LOG.infof("Evento 'solicitud.aprobada' recibido: %s", mensaje);
        procesarEvento(mensaje, EstadoSolicitud.APROBADA);
    }

    /**
     * Consume el evento 'solicitud.rechazada' producido por ms-aprobaciones.
     * Canal: solicitud-rechazada → topic: solicitud.rechazada
     */
    @Incoming("solicitud-rechazada")
    public void onSolicitudRechazada(String mensaje) {
        LOG.infof("Evento 'solicitud.rechazada' recibido: %s", mensaje);
        procesarEvento(mensaje, EstadoSolicitud.RECHAZADA);
    }

    private void procesarEvento(String mensaje, EstadoSolicitud nuevoEstado) {
        try {
            var evento = objectMapper.readValue(mensaje, AprobacionEventoDTO.class);
            solicitudUseCase.actualizarEstado(evento.solicitudId(), nuevoEstado);
            LOG.infof("Estado de solicitud %d actualizado a %s", evento.solicitudId(), nuevoEstado);
        } catch (JsonProcessingException e) {
            LOG.errorf("Error deserializando evento de aprobación: %s", e.getMessage());
        }
    }
}

