package com.vacaciones.politicas.exception;

public record ErrorResponse(
        String hora,
        String mensaje,
        String url,
        String codeStatus) {
}