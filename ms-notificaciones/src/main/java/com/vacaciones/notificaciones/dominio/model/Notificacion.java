package com.vacaciones.notificaciones.dominio.model;

import java.time.LocalDateTime;

public class Notificacion {

    private String id;
    private final String eventoId;
    private final TipoNotificacion tipo;
    private final Destinatario destinatario;
    private final String asunto;
    private final String cuerpo;
    private EstadoNotificacion estado;
    private final String eventoOrigen;
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;

    public Notificacion(
            String eventoId,
            TipoNotificacion tipo,
            Destinatario destinatario,
            String asunto,
            String cuerpo,
            String eventoOrigen) {
        if (asunto == null || asunto.isEmpty()) {
            throw new IllegalArgumentException("asunto no puede ser null ni vacio");
        }
        if (cuerpo == null || cuerpo.isEmpty()) {
            throw new IllegalArgumentException("cuerpo no puede ser null ni vacio");
        }

        this.eventoId = eventoId;
        this.tipo = tipo;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.cuerpo = cuerpo;
        this.eventoOrigen = eventoOrigen;
        this.estado = EstadoNotificacion.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaEnvio = null;
    }

    /**
     * Constructor de reconstitucion: reconstruye una Notificacion ya existente
     * (con su estado real) a partir de una fuente confiable, como persistencia.
     * No revalida invariantes de negocio - eso ya ocurrio al crearla la primera vez.
     */
    public Notificacion(
            String id,
            String eventoId,
            TipoNotificacion tipo,
            Destinatario destinatario,
            String asunto,
            String cuerpo,
            EstadoNotificacion estado,
            String eventoOrigen,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaEnvio) {
        this.id = id;
        this.eventoId = eventoId;
        this.tipo = tipo;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.cuerpo = cuerpo;
        this.estado = estado;
        this.eventoOrigen = eventoOrigen;
        this.fechaCreacion = fechaCreacion;
        this.fechaEnvio = fechaEnvio;
    }

    public void marcarComoEnviada() {
        if (estado == EstadoNotificacion.ENVIADO) {
            throw new EstadoNotificacionInvalidoException(
                    "La notificacion ya fue enviada, no se puede enviar dos veces");
        }
        this.estado = EstadoNotificacion.ENVIADO;
        this.fechaEnvio = LocalDateTime.now();
    }

    public void marcarComoFallida() {
        if (estado != EstadoNotificacion.PENDIENTE) {
            throw new EstadoNotificacionInvalidoException(
                    "Solo una notificacion PENDIENTE puede marcarse como fallida, estado actual: " + estado);
        }
        this.estado = EstadoNotificacion.FALLIDO;
    }

    public String getId() {
        return id;
    }

    public String getEventoId() {
        return eventoId;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public Destinatario getDestinatario() {
        return destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public String getEventoOrigen() {
        return eventoOrigen;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
