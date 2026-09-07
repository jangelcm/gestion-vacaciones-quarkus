package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import com.vacaciones.notificaciones.dominio.port.out.ResolverUsuarioPort;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudAprobadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.mapper.EventoNotificacionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudAprobadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudAprobadaConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventoNotificacionMapper eventoNotificacionMapper;
    private final EnviarNotificacionUseCase enviarNotificacionUseCase;
    private final ResolverUsuarioPort resolverUsuarioPort;

    public SolicitudAprobadaConsumer(
            ObjectMapper objectMapper,
            EventoNotificacionMapper eventoNotificacionMapper,
            EnviarNotificacionUseCase enviarNotificacionUseCase,
            ResolverUsuarioPort resolverUsuarioPort) {
        this.objectMapper = objectMapper;
        this.eventoNotificacionMapper = eventoNotificacionMapper;
        this.enviarNotificacionUseCase = enviarNotificacionUseCase;
        this.resolverUsuarioPort = resolverUsuarioPort;
    }

    @Incoming("solicitud-aprobada-in")
    public void onSolicitudAprobada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.aprobada' recibido: %s", mensaje);
        SolicitudAprobadaEvent evento = objectMapper.readValue(mensaje, SolicitudAprobadaEvent.class);
        UsuarioInfo usuario = resolverUsuarioPort.resolverPorColaboradorId(evento.colaboradorId())
                .orElseGet(() -> new UsuarioInfo(evento.colaboradorId(), null, "Colaborador " + evento.colaboradorId()));
        Notificacion notificacion = eventoNotificacionMapper.paraSolicitudAprobada(evento, usuario);
        enviarNotificacionUseCase.enviar(notificacion);
    }
}
