package com.jacm.solicitudes.domain.model;

public enum EstadoSolicitud {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada");

    private final String estado;

    EstadoSolicitud(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }
}
