package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import com.vacaciones.notificaciones.dominio.port.out.ResolverUsuarioPort;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudRechazadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.mapper.EventoNotificacionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudRechazadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudRechazadaConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventoNotificacionMapper eventoNotificacionMapper;
    private final EnviarNotificacionUseCase enviarNotificacionUseCase;
    private final ResolverUsuarioPort resolverUsuarioPort;

    public SolicitudRechazadaConsumer(
            ObjectMapper objectMapper,
            EventoNotificacionMapper eventoNotificacionMapper,
            EnviarNotificacionUseCase enviarNotificacionUseCase,
            ResolverUsuarioPort resolverUsuarioPort) {
        this.objectMapper = objectMapper;
        this.eventoNotificacionMapper = eventoNotificacionMapper;
        this.enviarNotificacionUseCase = enviarNotificacionUseCase;
        this.resolverUsuarioPort = resolverUsuarioPort;
    }

    @Incoming("solicitud-rechazada-in")
    public void onSolicitudRechazada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.rechazada' recibido: %s", mensaje);
        SolicitudRechazadaEvent evento = objectMapper.readValue(mensaje, SolicitudRechazadaEvent.class);
        UsuarioInfo usuario = resolverUsuarioPort.resolverPorColaboradorId(evento.colaboradorId())
                .orElseGet(() -> new UsuarioInfo(evento.colaboradorId(), null, "Colaborador " + evento.colaboradorId()));
        Notificacion notificacion = eventoNotificacionMapper.paraSolicitudRechazada(evento, usuario);
        enviarNotificacionUseCase.enviar(notificacion);
    }
}
