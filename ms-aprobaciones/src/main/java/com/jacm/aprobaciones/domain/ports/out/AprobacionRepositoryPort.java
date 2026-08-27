package com.jacm.aprobaciones.domain.ports.out;

import com.jacm.aprobaciones.domain.model.Aprobacion;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: abstracción de persistencia para Aprobacion.
 * Implementado en infrastructure/adapters/out/persistence/.
 */
public interface AprobacionRepositoryPort {

    /** Persiste o actualiza una aprobación. Devuelve la entidad con ID asignado. */
    Aprobacion guardar(Aprobacion aprobacion);

    Optional<Aprobacion> buscarPorSolicitudId(Long solicitudId);

    Optional<Aprobacion> buscarPorId(Long id);

    List<Aprobacion> listarPendientes();
}
