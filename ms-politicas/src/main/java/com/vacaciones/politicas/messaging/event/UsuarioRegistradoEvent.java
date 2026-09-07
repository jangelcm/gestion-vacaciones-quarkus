package com.vacaciones.politicas.messaging.event;

public record UsuarioRegistradoEvent(
        Long colaboradorId,
        String username,
        String email) {
}
