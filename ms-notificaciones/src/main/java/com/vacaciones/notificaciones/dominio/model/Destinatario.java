package com.vacaciones.notificaciones.dominio.model;

public record Destinatario(Long colaboradorId, String email, String nombre) {

    public Destinatario {
        if (colaboradorId == null) {
            throw new IllegalArgumentException("colaboradorId no puede ser null");
        }
        if (email == null || email.isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("email invalido: " + email);
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("nombre no puede ser null ni vacio");
        }
    }
}
