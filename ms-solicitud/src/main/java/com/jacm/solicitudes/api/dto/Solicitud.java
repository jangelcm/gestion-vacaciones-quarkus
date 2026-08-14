package com.jacm.solicitudes.api.dto;

import com.jacm.solicitudes.domain.model.EstadoSolicitud;

import java.time.LocalDate;

public class Solicitud {

    private Long id;

    private String colaboradorId;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private LocalDate fechaSolicitud;

    private EstadoSolicitud estado;

    public Solicitud(Long id, String colaboradorId, LocalDate fechaInicio, LocalDate fechaFin, LocalDate fechaSolicitud, EstadoSolicitud estado) {
        this.id = id;
        this.colaboradorId = colaboradorId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
    }

    public Solicitud(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColaboradorId() {
        return colaboradorId;
    }

    public void setColaboradorId(String colaboradorId) {
        this.colaboradorId = colaboradorId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}
