package com.vacaciones.politicas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.politicas.messaging.event.SolicitudCanceladaEvent;
import com.vacaciones.politicas.service.SaldoDiasService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudCanceladaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudCanceladaConsumer.class);

    private final ObjectMapper objectMapper;
    private final SaldoDiasService saldoDiasService;

    public SolicitudCanceladaConsumer(ObjectMapper objectMapper, SaldoDiasService saldoDiasService) {
        this.objectMapper = objectMapper;
        this.saldoDiasService = saldoDiasService;
    }

    @Incoming("solicitud-cancelada-in")
    public void onSolicitudCancelada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.cancelada' recibido: %s", mensaje);
        SolicitudCanceladaEvent evento = objectMapper.readValue(mensaje, SolicitudCanceladaEvent.class);
        saldoDiasService.procesarSolicitudCancelada(evento);
    }
}
