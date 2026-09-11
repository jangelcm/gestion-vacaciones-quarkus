package com.jacm.solicitudes.api.exception;

public record ErrorResponse(
        String hora,
        String mensaje,
        String url,
        String codeStatus) {
}
