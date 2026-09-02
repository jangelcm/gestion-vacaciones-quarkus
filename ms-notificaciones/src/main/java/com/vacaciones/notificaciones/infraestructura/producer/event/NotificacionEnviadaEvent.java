package com.vacaciones.notificaciones.infraestructura.producer.event;

import com.vacaciones.notificaciones.dominio.model.EstadoNotificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;
import java.time.LocalDateTime;

public record NotificacionEnviadaEvent(
        String notificacionId,
        Long colaboradorId,
        TipoNotificacion tipo,
        EstadoNotificacion estado,
        String eventoOrigen,
        LocalDateTime fechaEvento) {
}
