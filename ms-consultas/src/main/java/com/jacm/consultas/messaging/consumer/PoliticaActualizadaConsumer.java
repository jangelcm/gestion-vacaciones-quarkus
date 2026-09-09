package com.jacm.consultas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacm.consultas.messaging.dto.PoliticaActualizadaEvent;
import com.jacm.consultas.service.ConsultasProjectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PoliticaActualizadaConsumer {

    private static final Logger LOG = Logger.getLogger(PoliticaActualizadaConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ConsultasProjectionService consultasProjectionService;

    @Incoming("politica-actualizada-in")
    public void onPoliticaActualizada(String mensaje) {
        try {
            PoliticaActualizadaEvent event = objectMapper.readValue(mensaje, PoliticaActualizadaEvent.class);
            consultasProjectionService.proyectarPoliticaActualizada(event);
            LOG.infof("Read model de politica actualizado para politicaId=%d", event.politicaId());
        } catch (JsonProcessingException e) {
            LOG.errorf("Error deserializando politica.actualizada: %s", e.getMessage());
        }
    }
}
