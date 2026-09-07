package com.vacaciones.notificaciones.aplicacion.casouso;

import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.port.in.EnviarNotificacionUseCase;
import com.vacaciones.notificaciones.dominio.port.out.EnviadorEmailPort;
import com.vacaciones.notificaciones.dominio.port.out.NotificacionEventoPublisherPort;
import com.vacaciones.notificaciones.dominio.port.out.NotificacionRepositoryPort;
import com.vacaciones.notificaciones.dominio.port.out.NotificadorTiempoRealPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EnviarNotificacionService implements EnviarNotificacionUseCase {

    private static final Logger LOG = Logger.getLogger(EnviarNotificacionService.class);

    private final NotificacionRepositoryPort repository;
    private final EnviadorEmailPort enviadorEmailPort;
    private final NotificadorTiempoRealPort notificadorTiempoRealPort;
    private final NotificacionEventoPublisherPort eventoPublisherPort;

    public EnviarNotificacionService(
            NotificacionRepositoryPort repository,
            EnviadorEmailPort enviadorEmailPort,
            NotificadorTiempoRealPort notificadorTiempoRealPort,
            NotificacionEventoPublisherPort eventoPublisherPort) {
        this.repository = repository;
        this.enviadorEmailPort = enviadorEmailPort;
        this.notificadorTiempoRealPort = notificadorTiempoRealPort;
        this.eventoPublisherPort = eventoPublisherPort;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        if (notificacion.getEventoId() != null && repository.existePorEventoId(notificacion.getEventoId())) {
            return;
        }

        try {
            enviarSegunTipo(notificacion);
            notificacion.marcarComoEnviada();
        } catch (RuntimeException e) {
            notificacion.marcarComoFallida();
        }

        Notificacion guardada = repository.guardar(notificacion);
        eventoPublisherPort.publicarResultado(guardada);
    }

    private void enviarSegunTipo(Notificacion notificacion) {
        switch (notificacion.getTipo()) {
            case EMAIL -> enviarEmail(notificacion);
            case WEBSOCKET -> notificarWebsocket(notificacion);
            case RECORDATORIO -> {
                // El WebSocket va primero: es el canal critico en tiempo real (campanita) y no
                // debe esperar al email, que es best-effort y puede fallar o tardar (SMTP lento).
                notificarWebsocket(notificacion);
                intentarEnviarEmail(notificacion);
            }
        }
    }

    private void intentarEnviarEmail(Notificacion notificacion) {
        String email = notificacion.getDestinatario().email();
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            enviarEmail(notificacion);
        } catch (RuntimeException e) {
            LOG.warnf("No se pudo enviar el email de la notificacion %s, continua solo por WebSocket",
                    notificacion.getEventoId());
        }
    }

    private void enviarEmail(Notificacion notificacion) {
        enviadorEmailPort.enviar(notificacion.getDestinatario(), notificacion.getAsunto(), notificacion.getCuerpo());
    }

    private void notificarWebsocket(Notificacion notificacion) {
        notificadorTiempoRealPort.notificar(
                notificacion.getDestinatario().colaboradorId(),
                notificacion.getEventoOrigen(),
                notificacion);
    }
}
