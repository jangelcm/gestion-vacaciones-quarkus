package com.vacaciones.politicas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.politicas.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.politicas.service.SaldoDiasService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudAprobadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudAprobadaConsumer.class);

    private final ObjectMapper objectMapper;
    private final SaldoDiasService saldoDiasService;

    public SolicitudAprobadaConsumer(ObjectMapper objectMapper, SaldoDiasService saldoDiasService) {
        this.objectMapper = objectMapper;
        this.saldoDiasService = saldoDiasService;
    }

    @Incoming("solicitud-aprobada-in")
    public void onSolicitudAprobada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.aprobada' recibido: %s", mensaje);
        SolicitudAprobadaEvent evento = objectMapper.readValue(mensaje, SolicitudAprobadaEvent.class);
        saldoDiasService.procesarSolicitudAprobada(evento);
    }
}
