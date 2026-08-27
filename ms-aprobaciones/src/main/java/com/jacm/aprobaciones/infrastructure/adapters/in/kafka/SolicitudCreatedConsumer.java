package com.jacm.aprobaciones.infrastructure.adapters.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacm.aprobaciones.domain.ports.in.AprobarSolicitudUseCase;
import com.jacm.aprobaciones.infrastructure.adapters.in.kafka.dto.SolicitudCreadaEventDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudCreatedConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudCreatedConsumer.class);

    @Inject
    AprobarSolicitudUseCase aprobarSolicitudUseCase;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Consume el evento 'solicitud.creada' desde Kafka.
     * Registra la solicitud en el sistema de aprobaciones con estado PENDIENTE.
     *
     * Canal: solicitud-creada → topic: solicitud.creada
     */
    @Incoming("solicitud-creada")
    public void onSolicitudCreada(String mensaje) {
        LOG.infof("Evento 'solicitud.creada' recibido: %s", mensaje);
        try {
            SolicitudCreadaEventDTO evento = objectMapper.readValue(mensaje, SolicitudCreadaEventDTO.class);
            aprobarSolicitudUseCase.registrarParaAprobacion(evento.id());
            LOG.infof("Solicitud %d registrada para aprobación (estado: PENDIENTE)", evento.id());
        } catch (JsonProcessingException e) {
            LOG.errorf("Error deserializando evento 'solicitud.creada': %s", e.getMessage());
        }
    }
}
