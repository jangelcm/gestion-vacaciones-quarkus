package com.jacm.consultas.messaging.dto;

public record AprobacionEvento(
        Long solicitudId,
        String aprobadorId,
        String estado,
        String comentario) {
}
