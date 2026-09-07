package com.jacm.aprobaciones.application.usecases;

import com.jacm.aprobaciones.domain.model.Aprobacion;
import com.jacm.aprobaciones.domain.ports.in.AprobarSolicitudUseCase;
import com.jacm.aprobaciones.domain.ports.out.AprobacionEventPublisherPort;
import com.jacm.aprobaciones.domain.ports.out.AprobacionRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
    public Aprobacion registrarParaAprobacion(Long solicitudId, String colaboradorId, LocalDate fechaInicio, LocalDate fechaFin) {
        LOG.infof("Registrando solicitud %d para aprobación", solicitudId);
        var aprobacion = Aprobacion.nuevaPendiente(solicitudId, colaboradorId, fechaInicio, fechaFin);
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

        BigDecimal diasAprobados = calcularDiasHabiles(aprobacion.getFechaInicio(), aprobacion.getFechaFin());
        eventPublisherPort.publicarSolicitudAprobada(
                solicitudId, aprobacion.getColaboradorId(), diasAprobados, aprobadorId, comentario);

        return aprobacionGuardada;
    }

    private BigDecimal calcularDiasHabiles(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return BigDecimal.ZERO;
        }
        long diasHabiles = 0;
        LocalDate fecha = fechaInicio;
        while (!fecha.isAfter(fechaFin)) {
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasHabiles++;
            }
            fecha = fecha.plusDays(1);
        }
        return BigDecimal.valueOf(diasHabiles);
    }
}
