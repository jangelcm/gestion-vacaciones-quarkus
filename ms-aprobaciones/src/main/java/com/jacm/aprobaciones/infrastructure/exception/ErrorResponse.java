package com.jacm.aprobaciones.infrastructure.exception;

public record ErrorResponse(
        String hora,
        String mensaje,
        String url,
        String codeStatus) {
}
