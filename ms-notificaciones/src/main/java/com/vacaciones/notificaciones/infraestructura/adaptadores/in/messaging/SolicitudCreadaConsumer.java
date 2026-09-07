package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacaciones.notificaciones.dominio.model.UsuarioInfo;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import com.vacaciones.notificaciones.dominio.port.out.ResolverUsuarioPort;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event.SolicitudCreadaEvent;
import com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.mapper.EventoNotificacionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SolicitudCreadaConsumer {

    private static final Logger LOG = Logger.getLogger(SolicitudCreadaConsumer.class);

    private static final String ROL_APROBADOR = "Administrador";

    private final ObjectMapper objectMapper;
    private final EventoNotificacionMapper eventoNotificacionMapper;
    private final EnviarNotificacionUseCase enviarNotificacionUseCase;
    private final ResolverUsuarioPort resolverUsuarioPort;

    public SolicitudCreadaConsumer(
            ObjectMapper objectMapper,
            EventoNotificacionMapper eventoNotificacionMapper,
            EnviarNotificacionUseCase enviarNotificacionUseCase,
            ResolverUsuarioPort resolverUsuarioPort) {
        this.objectMapper = objectMapper;
        this.eventoNotificacionMapper = eventoNotificacionMapper;
        this.enviarNotificacionUseCase = enviarNotificacionUseCase;
        this.resolverUsuarioPort = resolverUsuarioPort;
    }

    @Incoming("solicitud-creada-in")
    public void onSolicitudCreada(String mensaje) throws JsonProcessingException {
        LOG.infof("Evento 'solicitud.creada' recibido: %s", mensaje);
        SolicitudCreadaEvent evento = objectMapper.readValue(mensaje, SolicitudCreadaEvent.class);
        List<UsuarioInfo> aprobadores = resolverUsuarioPort.resolverPorRol(ROL_APROBADOR);
        eventoNotificacionMapper.paraSolicitudCreada(evento, aprobadores)
                .forEach(enviarNotificacionUseCase::enviar);
    }
}
