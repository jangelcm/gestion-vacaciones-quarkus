package com.vacaciones.notificaciones.dominio.model;

/** Archivo adjunto opcional de un email (ej. el comprobante de vacaciones en PDF). */
public record Adjunto(String nombreArchivo, byte[] contenido, String contentType) {
}
