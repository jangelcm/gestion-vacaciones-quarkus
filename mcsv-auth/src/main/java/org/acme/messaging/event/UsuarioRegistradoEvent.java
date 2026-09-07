package org.acme.messaging.event;

public record UsuarioRegistradoEvent(
        Long colaboradorId,
        String username,
        String email) {
}
