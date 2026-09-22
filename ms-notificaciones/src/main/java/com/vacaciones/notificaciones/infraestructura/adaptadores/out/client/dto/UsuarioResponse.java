package com.vacaciones.notificaciones.infraestructura.adaptadores.out.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

// Deserializado por el REST client que llama a mcsv-auth; necesita reflexion Jackson
// igual que un DTO de respuesta de un endpoint propio.
@RegisterForReflection
public record UsuarioResponse(Long id, String username, String email, String telefono, Boolean isActive) {
}
