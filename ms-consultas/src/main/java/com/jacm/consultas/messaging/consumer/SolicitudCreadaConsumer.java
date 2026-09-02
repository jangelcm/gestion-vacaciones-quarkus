package com.jacm.consultas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacm.consultas.messaging.dto.SolicitudCreadaEvent;
import com.jacm.consultas.service.ConsultasProjectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudCreadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudCreadaConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ConsultasProjectionService consultasProjectionService;

    @Incoming("solicitud-creada-in")
    public void onSolicitudCreada(String mensaje) {
        try {
            SolicitudCreadaEvent event = objectMapper.readValue(mensaje, SolicitudCreadaEvent.class);
            consultasProjectionService.proyectarSolicitudCreada(event);
            LOG.infof("Read model actualizado para solicitud.creada id=%d", event.id());
        } catch (JsonProcessingException e) {
            LOG.errorf("Error deserializando solicitud.creada: %s", e.getMessage());
        }
    }
}
