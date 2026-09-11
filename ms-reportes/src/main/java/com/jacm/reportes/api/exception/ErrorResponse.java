package com.jacm.reportes.api.exception;

public record ErrorResponse(
        String hora,
        String mensaje,
        String url,
        String codeStatus) {
}
