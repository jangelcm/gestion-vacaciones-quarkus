package com.jacm.solicitudes.domain.service;


import com.jacm.solicitudes.api.dto.Solicitud;
import com.jacm.solicitudes.domain.model.EstadoSolicitud;
import com.jacm.solicitudes.domain.ports.out.SolicitudEventPublisherPort;
import com.jacm.solicitudes.domain.ports.out.SolicitudRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@ApplicationScoped
@Transactional
public class SolicitudServiceImpl implements SolicitudService {

    @Inject
    SolicitudRepositoryPort solicitudRepositoryPort;

    @Inject
    SolicitudEventPublisherPort solicitudEventPublisherPort;

    @Override
    public Solicitud crearSolicitud(Solicitud solicitud) {
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        var solicitudGuardada = solicitudRepositoryPort.guardar(solicitud);
        solicitudEventPublisherPort.publicarSolicitudCreada(solicitudGuardada);
        return solicitudGuardada;
    }

    @Override
    public Solicitud obtenerPorId(Long id) {
        return solicitudRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada con id: " + id));
    }

    @Override
    public List<Solicitud> listarPorColaborador(Long colaboradorId) {
        return solicitudRepositoryPort.listarPorColaboradorId(String.valueOf(colaboradorId));
    }

    @Override
    public void actualizarEstado(Long id, EstadoSolicitud nuevoEstado) {
        solicitudRepositoryPort.actualizarEstado(id, nuevoEstado);
    }
}

