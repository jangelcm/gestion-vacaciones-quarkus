package com.vacaciones.politicas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.politicas.messaging.event.UsuarioRegistradoEvent;
import com.vacaciones.politicas.service.SaldoDiasService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UsuarioRegistradoConsumer {

    private static final Logger LOG = Logger.getLogger(UsuarioRegistradoConsumer.class);

    private final ObjectMapper objectMapper;
    private final SaldoDiasService saldoDiasService;

    public UsuarioRegistradoConsumer(ObjectMapper objectMapper, SaldoDiasService saldoDiasService) {
        this.objectMapper = objectMapper;
        this.saldoDiasService = saldoDiasService;
    }

    @Incoming("usuario-registrado-in")
    public void onUsuarioRegistrado(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'usuario.registrado' recibido: %s", mensaje);
        UsuarioRegistradoEvent evento = objectMapper.readValue(mensaje, UsuarioRegistradoEvent.class);
        saldoDiasService.asignarPoliticaPorDefectoSiNoTiene(evento.colaboradorId());
    }
}
