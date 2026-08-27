package com.jacm.aprobaciones.application.usecases;

import com.jacm.aprobaciones.domain.model.Aprobacion;
import com.jacm.aprobaciones.domain.ports.in.AprobarSolicitudUseCase;
import com.jacm.aprobaciones.domain.ports.out.AprobacionEventPublisherPort;
import com.jacm.aprobaciones.domain.ports.out.AprobacionRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.NoSuchElementException;

@ApplicationScoped
public class AprobarSolicitudUseCaseImpl implements AprobarSolicitudUseCase {

    private static final Logger LOG = Logger.getLogger(AprobarSolicitudUseCaseImpl.class);

    @Inject
    AprobacionRepositoryPort repositoryPort;

    @Inject
    AprobacionEventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    public Aprobacion registrarParaAprobacion(Long solicitudId) {
        LOG.infof("Registrando solicitud %d para aprobación", solicitudId);
        var aprobacion = Aprobacion.nuevaPendiente(solicitudId);
        return repositoryPort.guardar(aprobacion);
    }

    @Override
    @Transactional
    public Aprobacion aprobar(Long solicitudId, String aprobadorId, String comentario) {
        LOG.infof("Aprobando solicitud %d por aprobador %s", solicitudId, aprobadorId);

        var aprobacion = repositoryPort.buscarPorSolicitudId(solicitudId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe una aprobación pendiente para la solicitud: " + solicitudId));

        aprobacion.aprobar(aprobadorId, comentario);
        var aprobacionGuardada = repositoryPort.guardar(aprobacion);
        eventPublisherPort.publicarSolicitudAprobada(solicitudId, aprobadorId, comentario);

        return aprobacionGuardada;
    }
}
