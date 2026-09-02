package com.vacaciones.notificaciones.infraestructura.adaptadores.in.rest.dto.request;

import com.vacaciones.notificaciones.dominio.model.TipoNotificacion;

public record NotificacionTestRequestDto(
        Long colaboradorId,
        String email,
        String nombre,
        TipoNotificacion tipo,
        String asunto,
        String cuerpo) {
}
