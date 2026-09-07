package com.vacaciones.notificaciones.infraestructura.adaptadores.in.messaging.event;

import java.time.LocalDate;

/**
 * ms-solicitud publica el objeto Solicitud crudo al topic 'solicitud.creada' (no un DTO de
 * evento dedicado), por eso el campo se llama "id" y no "solicitudId"/"eventoId". No trae
 * datos del aprobador: quien debe ser notificado (todos los usuarios con rol Administrador)
 * se resuelve en el consumer via ResolverUsuarioPort, no desde el evento.
 */
public record SolicitudCreadaEvent(
        Long id,
        String colaboradorId,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}
