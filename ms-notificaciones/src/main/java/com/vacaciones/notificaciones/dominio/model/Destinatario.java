package com.vacaciones.notificaciones.dominio.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

// Anidado dentro de Notificacion al serializar con Jackson para el push por WebSocket;
// necesita la misma anotacion que Notificacion para imagen nativa (ver ahi el porque).
@RegisterForReflection
public record Destinatario(Long colaboradorId, String email, String nombre) {

    public Destinatario {
        if (colaboradorId == null) {
            throw new IllegalArgumentException("colaboradorId no puede ser null");
        }
        // El email es opcional: un colaborador sin email registrado aun puede recibir
        // la notificacion por WebSocket (campanita); solo se valida el formato si viene presente.
        if (email != null && !email.isEmpty() && !email.contains("@")) {
            throw new IllegalArgumentException("email invalido: " + email);
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("nombre no puede ser null ni vacio");
        }
    }
}
