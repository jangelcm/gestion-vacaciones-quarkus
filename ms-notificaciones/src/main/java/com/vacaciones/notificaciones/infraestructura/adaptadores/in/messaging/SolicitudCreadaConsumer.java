package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudCreadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.mapper.EventoNotificacionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudCreadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudCreadaConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventoNotificacionMapper eventoNotificacionMapper;
    private final EnviarNotificacionUseCase enviarNotificacionUseCase;

    public SolicitudCreadaConsumer(
            ObjectMapper objectMapper,
            EventoNotificacionMapper eventoNotificacionMapper,
            EnviarNotificacionUseCase enviarNotificacionUseCase) {
        this.objectMapper = objectMapper;
        this.eventoNotificacionMapper = eventoNotificacionMapper;
        this.enviarNotificacionUseCase = enviarNotificacionUseCase;
    }

    @Incoming("solicitud-creada-in")
    public void onSolicitudCreada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.creada' recibido: %s", mensaje);
        SolicitudCreadaEvent evento = objectMapper.readValue(mensaje, SolicitudCreadaEvent.class);
        Notificacion notificacion = eventoNotificacionMapper.paraSolicitudCreada(evento);
        enviarNotificacionUseCase.enviar(notificacion);
    }
}
