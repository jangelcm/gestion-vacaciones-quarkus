package com.jacm.consultas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacm.consultas.messaging.dto.DiasDisponiblesActualizadosEvent;
import com.jacm.consultas.service.ConsultasProjectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DiasDisponiblesConsumer {

    private static final Logger LOG = Logger.getLogger(DiasDisponiblesConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ConsultasProjectionService consultasProjectionService;

    @Incoming("dias-disponibles-actualizados-in")
    public void onDiasDisponiblesActualizados(String mensaje) {
        try {
            DiasDisponiblesActualizadosEvent event = objectMapper.readValue(mensaje, DiasDisponiblesActualizadosEvent.class);
            consultasProjectionService.proyectarSaldoActualizado(event);
            LOG.infof("Read model de saldos actualizado para colaborador=%d", event.colaboradorId());
        } catch (JsonProcessingException e) {
            LOG.errorf("Error deserializando dias.disponibles.actualizados: %s", e.getMessage());
        }
    }
}
