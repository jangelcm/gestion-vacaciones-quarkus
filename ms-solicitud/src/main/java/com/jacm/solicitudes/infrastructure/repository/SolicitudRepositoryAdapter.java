package com.jacm.solicitudes.infrastructure.repository;

import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.domain.model.EstadoSolicitud;
import com.jacm.solicitudes.domain.model.SolicitudEntity;
import com.jacm.solicitudes.domain.ports.out.SolicitudRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia: implementa {@link SolicitudRepositoryPort}
 * usando Panache/JPA a través de {@link SolicitudRepository}.
 * Traduce entre el modelo de dominio {@link Solicitud} y la entidad JPA {@link SolicitudEntity}.
 */
@ApplicationScoped
public class SolicitudRepositoryAdapter implements SolicitudRepositoryPort {

    @Inject
    SolicitudRepository panacheRepository;

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        var entity = SolicitudEntity.fromDomain(solicitud);
        panacheRepository.persist(entity);
        solicitud.setId(entity.id);
        return solicitud;
    }

    @Override
    public Optional<Solicitud> buscarPorId(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(SolicitudEntity::toDomain);
    }

    @Override
    public List<Solicitud> listarPorColaboradorId(String colaboradorId) {
        return panacheRepository.list("colaboradorId", colaboradorId)
                .stream()
                .map(SolicitudEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void actualizarEstado(Long id, EstadoSolicitud nuevoEstado) {
        panacheRepository.findByIdOptional(id)
                .ifPresent(entity -> entity.setEstado(nuevoEstado));
    }
}
