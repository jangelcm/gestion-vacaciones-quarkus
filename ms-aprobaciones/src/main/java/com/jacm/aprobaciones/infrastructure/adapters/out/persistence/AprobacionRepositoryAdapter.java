package com.jacm.aprobaciones.infrastructure.adapters.out.persistence;

import com.jacm.aprobaciones.domain.model.Aprobacion;
import com.jacm.aprobaciones.domain.ports.out.AprobacionRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia: implementa el puerto de salida {@link AprobacionRepositoryPort}
 * usando Panache/JPA. Traduce entre modelo de dominio y entidad JPA.
 */
@ApplicationScoped
public class AprobacionRepositoryAdapter implements AprobacionRepositoryPort {

    @Inject
    AprobacionPanacheRepository panacheRepository;

    @Override
    public Aprobacion guardar(Aprobacion aprobacion) {
        var entity = AprobacionJpaEntity.fromDomain(aprobacion);
        if (entity.id == null) {
            panacheRepository.persist(entity);
        } else {
            entity = panacheRepository.getEntityManager().merge(entity);
        }
        return entity.toDomain();
    }

    @Override
    public Optional<Aprobacion> buscarPorSolicitudId(Long solicitudId) {
        return panacheRepository.findBySolicitudId(solicitudId)
                .map(AprobacionJpaEntity::toDomain);
    }

    @Override
    public Optional<Aprobacion> buscarPorId(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(AprobacionJpaEntity::toDomain);
    }

    @Override
    public List<Aprobacion> listarPendientes() {
        return panacheRepository.findAllPendientes()
                .stream()
                .map(AprobacionJpaEntity::toDomain)
                .toList();
    }
}
