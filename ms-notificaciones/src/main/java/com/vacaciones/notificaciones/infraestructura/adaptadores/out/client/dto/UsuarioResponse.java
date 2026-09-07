package com.vacaciones.notificaciones.infraestructura.adaptadores.out.client.dto;

public record UsuarioResponse(Long id, String username, String email, String telefono, Boolean isActive) {
}
