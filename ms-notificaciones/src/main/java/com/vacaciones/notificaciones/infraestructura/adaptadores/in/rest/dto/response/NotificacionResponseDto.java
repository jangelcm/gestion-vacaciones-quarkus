package com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest.dto.response;

import com.vacaciones.notificaciones.dominio.model.Destinatario;
import com.vacaciones.notificaciones.dominio.model.EstadoNotificacion;
import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;

public record NotificacionResponseDto(
        String id,
        String eventoId,
        TipoNotificacion tipo,
        Destinatario destinatario,
        String asunto,
        String cuerpo,
        EstadoNotificacion estado,
        String eventoOrigen,
        String fechaCreacion,
        String fechaEnvio) {
}
