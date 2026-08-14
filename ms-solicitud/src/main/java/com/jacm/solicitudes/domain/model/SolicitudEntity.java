package com.jacm.solicitudes.domain.model;

import com.jacm.solicitudes.api.dto.Solicitud;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "solicitudes")
public class SolicitudEntity extends PanacheEntity {

    @Column(name = "colaborador_id", nullable = false)
    private String colaboradorId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @CreationTimestamp
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    public static SolicitudEntity fromDomain(Solicitud domain) {
        SolicitudEntity entity = new SolicitudEntity();
        entity.id = domain.getId();
        entity.colaboradorId = domain.getColaboradorId();
        entity.fechaInicio = domain.getFechaInicio();
        entity.fechaFin = domain.getFechaFin();
        entity.estado = domain.getEstado();
        entity.fechaSolicitud = domain.getFechaSolicitud();
        return entity;
    }

    public Solicitud toDomain() {
        return new Solicitud(this.id, this.colaboradorId, this.fechaInicio, this.fechaFin, this.fechaSolicitud, this.estado);
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
