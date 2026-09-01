package com.vacaciones.notificaciones.infraestructura.producer;

import com.vacaciones.notificaciones.dominio.model.Notificacion;
import com.vacaciones.notificaciones.dominio.port.out.NotificacionEventoPublisherPort;
import com.vacaciones.notificaciones.infraestructura.producer.event.NotificacionEnviadaEvent;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class NotificacionEnviadaEventProducer implements NotificacionEventoPublisherPort {

    private final Emitter<NotificacionEnviadaEvent> emitter;

    public NotificacionEnviadaEventProducer(
            @Channel("notificacion-enviada-out") Emitter<NotificacionEnviadaEvent> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void publicarResultado(Notificacion notificacion) {
        emitter.send(new NotificacionEnviadaEvent(
                notificacion.getId(),
                notificacion.getDestinatario().colaboradorId(),
                notificacion.getTipo(),
                notificacion.getEstado(),
                notificacion.getEventoOrigen(),
                LocalDateTime.now()));
    }
}
