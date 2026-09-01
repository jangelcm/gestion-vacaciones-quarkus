package com.vacaciones.notificaciones.infraestructura.exception;

public record ErrorResponse(
        String hora,
        String mensaje,
        String url,
        String codeStatus) {
}
