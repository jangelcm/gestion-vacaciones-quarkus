package com.jacm.consultas.api.exception;

public record ErrorResponse(
        String hora,
        String mensaje,
        String url,
        String codeStatus) {
}
