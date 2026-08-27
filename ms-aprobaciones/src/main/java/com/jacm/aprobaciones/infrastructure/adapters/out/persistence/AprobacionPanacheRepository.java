package com.jacm.aprobaciones.infrastructure.adapters.out.persistence;

import com.jacm.aprobaciones.domain.model.EstadoAprobacion;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Panache para {@link AprobacionJpaEntity}.
 * Proporciona las operaciones JPA de bajo nivel.
 * Usado internamente por {@link AprobacionRepositoryAdapter}.
 */
@ApplicationScoped
public class AprobacionPanacheRepository implements PanacheRepository<AprobacionJpaEntity> {

    public Optional<AprobacionJpaEntity> findBySolicitudId(Long solicitudId) {
        return find("solicitudId", solicitudId).firstResultOptional();
    }

    public List<AprobacionJpaEntity> findAllPendientes() {
        return list("estado", EstadoAprobacion.PENDIENTE);
    }
}
