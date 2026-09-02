package com.vacaciones.notificaciones.dominio.model;

public class EstadoNotificacionInvalidoException extends RuntimeException {

    public EstadoNotificacionInvalidoException(String mensaje) {
        super(mensaje);
    }
}
