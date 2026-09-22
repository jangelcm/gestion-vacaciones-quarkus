package com.jacm.solicitudes.infrastructure.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

// Deserializado por el REST client que llama a ms-politicas; necesita reflexion Jackson
// igual que un DTO de respuesta de un endpoint propio.
@RegisterForReflection
public record ValidarSolicitudResponse(
        boolean aprobado,
        long diasSolicitados,
        String motivoRechazo) {
}
