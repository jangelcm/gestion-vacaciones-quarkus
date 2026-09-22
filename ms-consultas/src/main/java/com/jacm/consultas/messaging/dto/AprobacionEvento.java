package com.jacm.consultas.messaging.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AprobacionEvento(
        Long solicitudId,
        String aprobadorId,
        String estado,
        String comentario) {
}
