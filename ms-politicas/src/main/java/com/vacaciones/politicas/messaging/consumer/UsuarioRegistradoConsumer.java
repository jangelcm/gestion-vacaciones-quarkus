package com.vacaciones.politicas.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.politicas.messaging.event.EmpleadoCreadoEvent;
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

    @Incoming("empleado-creado-in")
    public void onEmpleadoCreado(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'empleado.creado' recibido: %s", mensaje);
        EmpleadoCreadoEvent evento = objectMapper.readValue(mensaje, EmpleadoCreadoEvent.class);
        saldoDiasService.asignarPoliticaPorDefectoSiNoTiene(evento.trabajadorId(), evento.fechaIngreso());
    }

}
