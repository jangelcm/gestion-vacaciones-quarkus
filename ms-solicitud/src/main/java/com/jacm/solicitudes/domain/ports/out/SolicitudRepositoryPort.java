package com.jacm.solicitudes.domain.ports.out;

import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.domain.model.EstadoSolicitud;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: abstracción de persistencia para Solicitud.
 * Implementado en infrastructure/repository/SolicitudRepositoryAdapter.
 */
public interface SolicitudRepositoryPort {

    /** Persiste la solicitud y asigna el ID generado. */
    Solicitud guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(Long id);

    List<Solicitud> listarPorColaboradorId(String colaboradorId);

    void actualizarEstado(Long id, EstadoSolicitud nuevoEstado);
}
