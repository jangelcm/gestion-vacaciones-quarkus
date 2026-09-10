package com.vacaciones.politicas.service;

import com.vacaciones.politicas.entity.MovimientoSaldoEntity;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.BadRequestException;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.repository.MovimientoSaldoRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ApplicationScoped
public class SaldoDiasWriteOperations {

    static final String TIPO_RESERVA = "RESERVA";
    static final String TIPO_DESCUENTO = "DESCUENTO";
    static final String TIPO_DEVOLUCION = "DEVOLUCION";
    static final String ORIGEN_SOLICITUD_CREADA = "solicitud.creada";
    static final String ORIGEN_SOLICITUD_APROBADA = "solicitud.aprobada";
    static final String ORIGEN_SOLICITUD_CANCELADA = "solicitud.cancelada";
    static final int DIAS_PERIODO_ANUAL = 360;

    private final SaldoDiasRepository saldoDiasRepository;
    private final MovimientoSaldoRepository movimientoSaldoRepository;

    public SaldoDiasWriteOperations(
            SaldoDiasRepository saldoDiasRepository,
            MovimientoSaldoRepository movimientoSaldoRepository) {
        this.saldoDiasRepository = saldoDiasRepository;
        this.movimientoSaldoRepository = movimientoSaldoRepository;
    }

    @Transactional
    public SaldoDiasEntity ejecutarDescuento(
            Long colaboradorId,
            Long solicitudId,
            BigDecimal dias,
            String eventoOrigen,
            String eventoId) {
        if (movimientoSaldoRepository.existsByEventoId(eventoId)) {
            return null;
        }
        if (dias == null || dias.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El evento no incluye una cantidad de dias valida para descontar");
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);
        if (saldoDias == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para el colaborador");
        }

        normalizarCampos(saldoDias);

        // 1. Al aprobar, los días pasan de PENDIENTES a USADOS
        BigDecimal pendientesRestantes = saldoDias.getDiasPendientes().subtract(dias);
        saldoDias.setDiasPendientes(pendientesRestantes.max(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));
        saldoDias.setDiasUsados(saldoDias.getDiasUsados().add(dias));

        saldoDiasRepository.persist(saldoDias);
        saldoDiasRepository.getEntityManager().flush();

        movimientoSaldoRepository.persist(buildMovimiento(
                saldoDias,
                solicitudId,
                TIPO_DESCUENTO,
                dias,
                eventoOrigen,
                eventoId));

        return saldoDias;
    }

    @Transactional
    public SaldoDiasEntity reservarDiasPendientes(
            Long colaboradorId,
            Long solicitudId,
            BigDecimal dias,
            String eventoId) {
        if (movimientoSaldoRepository.existsByEventoId(eventoId)) {
            return null;
        }
        if (dias == null || dias.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El evento no incluye una cantidad de dias valida para reservar");
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);
        if (saldoDias == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para el colaborador");
        }

        normalizarCampos(saldoDias);
        saldoDias.setDiasPendientes(saldoDias.getDiasPendientes().add(dias));

        saldoDiasRepository.persist(saldoDias);
        saldoDiasRepository.getEntityManager().flush();

        movimientoSaldoRepository.persist(buildMovimiento(
                saldoDias,
                solicitudId,
                TIPO_RESERVA,
                dias,
                ORIGEN_SOLICITUD_CREADA,
                eventoId));

        return saldoDias;
    }

    @Transactional
    public SaldoDiasEntity ejecutarDevolucion(
            Long colaboradorId,
            Long solicitudId,
            BigDecimal dias,
            String eventoOrigen,
            String eventoId) {
        if (movimientoSaldoRepository.existsByEventoId(eventoId)) {
            return null;
        }
        if (dias == null || dias.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El evento no incluye una cantidad de dias valida para devolver");
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);
        if (saldoDias == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para el colaborador");
        }

        normalizarCampos(saldoDias);

        // 1. Al rechazar/cancelar, liberas los días pendientes
        BigDecimal diasPendientesRestantes = saldoDias.getDiasPendientes().subtract(dias);
        saldoDias.setDiasPendientes(diasPendientesRestantes.max(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));

        saldoDiasRepository.persist(saldoDias);
        saldoDiasRepository.getEntityManager().flush();

        movimientoSaldoRepository.persist(buildMovimiento(
                saldoDias,
                solicitudId,
                TIPO_DEVOLUCION,
                dias,
                eventoOrigen,
                eventoId));

        return saldoDias;
    }

    @Transactional
    public SaldoDiasEntity ejecutarProcesoDiario(SaldoDiasEntity saldo) {
        normalizarCampos(saldo);

        int trabajados = saldo.getDiasTrabajados() + 1;

        // Si cumple el ciclo/aniversario (360 días laborados)
        if (trabajados >= DIAS_PERIODO_ANUAL) {
            PoliticaEntity politica = saldo.getPolitica();
            BigDecimal diasGanadosAnio = (politica != null && politica.getDiasBaseAnio() != null)
                    ? BigDecimal.valueOf(politica.getDiasBaseAnio())
                    : new BigDecimal("30.0");

            BigDecimal nuevoAcumulado = saldo.getDiasAcumulados().add(diasGanadosAnio);

            if (politica != null && politica.getMaxDiasAcumulables() != null) {
                BigDecimal tope = BigDecimal.valueOf(politica.getMaxDiasAcumulables()).setScale(2, RoundingMode.HALF_UP);
                nuevoAcumulado = nuevoAcumulado.min(tope);
            }

            saldo.setDiasAcumulados(nuevoAcumulado);
            saldo.setDiasTrabajados(0); // Reinicia ciclo de días trabajados en el año actual
        } else {
            saldo.setDiasTrabajados(trabajados);
        }

        saldoDiasRepository.persist(saldo);
        saldoDiasRepository.getEntityManager().flush();
        return saldo;
    }

    private void normalizarCampos(SaldoDiasEntity saldoDias) {
        if (saldoDias.getDiasDisponibles() == null) {
            saldoDias.setDiasDisponibles(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (saldoDias.getDiasUsados() == null) {
            saldoDias.setDiasUsados(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (saldoDias.getDiasAcumulados() == null) {
            saldoDias.setDiasAcumulados(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (saldoDias.getDiasPendientes() == null) {
            saldoDias.setDiasPendientes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (saldoDias.getDiasTrabajados() == null) {
            saldoDias.setDiasTrabajados(0);
        }
    }

    private MovimientoSaldoEntity buildMovimiento(
            SaldoDiasEntity saldoDias,
            Long solicitudId,
            String tipoMovimiento,
            BigDecimal dias,
            String eventoOrigen,
            String eventoId) {
        return MovimientoSaldoEntity.builder()
                .saldo(saldoDias)
                .solicitudId(solicitudId)
                .tipoMovimiento(tipoMovimiento)
                .dias(dias)
                .eventoOrigen(eventoOrigen)
                .eventoId(eventoId)
                .build();
    }
}