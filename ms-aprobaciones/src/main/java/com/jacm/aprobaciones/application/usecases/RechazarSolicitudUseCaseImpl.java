package com.jacm.aprobaciones.application.usecases;

import com.jacm.aprobaciones.domain.model.Aprobacion;
import com.jacm.aprobaciones.domain.ports.in.RechazarSolicitudUseCase;
import com.jacm.aprobaciones.domain.ports.out.AprobacionEventPublisherPort;
import com.jacm.aprobaciones.domain.ports.out.AprobacionRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.NoSuchElementException;

@ApplicationScoped
public class RechazarSolicitudUseCaseImpl implements RechazarSolicitudUseCase {

    private static final Logger LOG = Logger.getLogger(RechazarSolicitudUseCaseImpl.class);

    @Inject
    AprobacionRepositoryPort repositoryPort;

    @Inject
    AprobacionEventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    public Aprobacion rechazar(Long solicitudId, String aprobadorId, String motivo) {
        LOG.infof("Rechazando solicitud %d por aprobador %s", solicitudId, aprobadorId);

        var aprobacion = repositoryPort.buscarPorSolicitudId(solicitudId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe una aprobación pendiente para la solicitud: " + solicitudId));

        aprobacion.rechazar(aprobadorId, motivo);
        var aprobacionGuardada = repositoryPort.guardar(aprobacion);
        eventPublisherPort.publicarSolicitudRechazada(solicitudId, aprobadorId, motivo);

        return aprobacionGuardada;
    }
}
