package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudCanceladaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.mapper.EventoNotificacionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudCanceladaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudCanceladaConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventoNotificacionMapper eventoNotificacionMapper;
    private final EnviarNotificacionUseCase enviarNotificacionUseCase;

    public SolicitudCanceladaConsumer(
            ObjectMapper objectMapper,
            EventoNotificacionMapper eventoNotificacionMapper,
            EnviarNotificacionUseCase enviarNotificacionUseCase) {
        this.objectMapper = objectMapper;
        this.eventoNotificacionMapper = eventoNotificacionMapper;
        this.enviarNotificacionUseCase = enviarNotificacionUseCase;
    }

    @Incoming("solicitud-cancelada-in")
    public void onSolicitudCancelada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.cancelada' recibido: %s", mensaje);
        SolicitudCanceladaEvent evento = objectMapper.readValue(mensaje, SolicitudCanceladaEvent.class);
        Notificacion notificacion = eventoNotificacionMapper.paraSolicitudCancelada(evento);
        enviarNotificacionUseCase.enviar(notificacion);
    }
}
