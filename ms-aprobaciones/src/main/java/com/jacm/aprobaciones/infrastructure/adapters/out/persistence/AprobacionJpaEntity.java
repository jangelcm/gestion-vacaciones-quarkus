package com.jacm.aprobaciones.infrastructure.adapters.out.persistence;

import com.jacm.aprobaciones.domain.model.Aprobacion;
import com.jacm.aprobaciones.domain.model.EstadoAprobacion;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'aprobaciones'.
 * Reside exclusivamente en la capa de infraestructura.
 * Mapea hacia/desde el modelo de dominio {@link Aprobacion}.
 */
@Entity
@Table(name = "aprobaciones")
public class AprobacionJpaEntity extends PanacheEntity {

    @Column(name = "solicitud_id", nullable = false, unique = true)
    public Long solicitudId;

    @Column(name = "colaborador_id")
    public String colaboradorId;

    @Column(name = "fecha_inicio")
    public LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    public LocalDate fechaFin;

    @Column(name = "aprobador_id")
    public String aprobadorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public EstadoAprobacion estado;

    @Column(columnDefinition = "TEXT")
    public String comentario;

    @CreationTimestamp
    @Column(name = "fecha_aprobacion")
    public LocalDateTime fechaAprobacion;

    @Column(name = "nivel_aprobacion", nullable = false)
    public int nivelAprobacion = 1;

    // -------------------------------------------------------------------------
    // Mapeo Dominio ↔ Infraestructura
    // -------------------------------------------------------------------------

    public static AprobacionJpaEntity fromDomain(Aprobacion domain) {
        var entity = new AprobacionJpaEntity();
        entity.id = domain.getId();
        entity.solicitudId = domain.getSolicitudId();
        entity.colaboradorId = domain.getColaboradorId();
        entity.fechaInicio = domain.getFechaInicio();
        entity.fechaFin = domain.getFechaFin();
        entity.aprobadorId = domain.getAprobadorId();
        entity.estado = domain.getEstado();
        entity.comentario = domain.getComentario();
        entity.fechaAprobacion = domain.getFechaAprobacion();
        entity.nivelAprobacion = domain.getNivelAprobacion();
        return entity;
    }

    public Aprobacion toDomain() {
        return new Aprobacion(id, solicitudId, colaboradorId, fechaInicio, fechaFin,
                aprobadorId, estado, comentario, fechaAprobacion, nivelAprobacion);
    }
}
