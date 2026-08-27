package com.jacm.aprobaciones.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio pura. Sin anotaciones JPA, Kafka ni HTTP.
 * Contiene la lógica de negocio de la aprobación (transiciones de estado).
 */
public class Aprobacion {

    private Long id;
    private Long solicitudId;
    private String aprobadorId;
    private EstadoAprobacion estado;
    private String comentario;
    private LocalDateTime fechaAprobacion;
    private int nivelAprobacion;

    public Aprobacion(Long id, Long solicitudId, String aprobadorId, EstadoAprobacion estado,
                      String comentario, LocalDateTime fechaAprobacion, int nivelAprobacion) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.aprobadorId = aprobadorId;
        this.estado = estado;
        this.comentario = comentario;
        this.fechaAprobacion = fechaAprobacion;
        this.nivelAprobacion = nivelAprobacion;
    }

    // -------------------------------------------------------------------------
    // Métodos de dominio
    // -------------------------------------------------------------------------

    /** Crea una nueva aprobación en estado PENDIENTE al recibir la solicitud. */
    public static Aprobacion nuevaPendiente(Long solicitudId) {
        return new Aprobacion(null, solicitudId, null, EstadoAprobacion.PENDIENTE, null, null, 1);
    }

    /** Transiciona el estado a APROBADO y registra la decisión. */
    public void aprobar(String aprobadorId, String comentario) {
        this.estado = EstadoAprobacion.APROBADO;
        this.aprobadorId = aprobadorId;
        this.comentario = comentario;
        this.fechaAprobacion = LocalDateTime.now();
    }

    /** Transiciona el estado a RECHAZADO y registra el motivo. */
    public void rechazar(String aprobadorId, String motivo) {
        this.estado = EstadoAprobacion.RECHAZADO;
        this.aprobadorId = aprobadorId;
        this.comentario = motivo;
        this.fechaAprobacion = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters y Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSolicitudId() { return solicitudId; }
    public void setSolicitudId(Long solicitudId) { this.solicitudId = solicitudId; }

    public String getAprobadorId() { return aprobadorId; }
    public void setAprobadorId(String aprobadorId) { this.aprobadorId = aprobadorId; }

    public EstadoAprobacion getEstado() { return estado; }
    public void setEstado(EstadoAprobacion estado) { this.estado = estado; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    public int getNivelAprobacion() { return nivelAprobacion; }
    public void setNivelAprobacion(int nivelAprobacion) { this.nivelAprobacion = nivelAprobacion; }
}
