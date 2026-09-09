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

@ApplicationScoped
public class SaldoDiasWriteOperations {

    static final String TIPO_DESCUENTO = "DESCUENTO";
    static final String TIPO_DEVOLUCION = "DEVOLUCION";
    static final String ORIGEN_SOLICITUD_APROBADA = "solicitud.aprobada";
    static final String ORIGEN_SOLICITUD_CANCELADA = "solicitud.cancelada";
    static final int DIAS_PERIODO_ANUAL = 360;
    static final BigDecimal DIAS_TRUNCOS_POR_PERIODO = new BigDecimal("30.0");

    private final SaldoDiasRepository saldoDiasRepository;
    private final MovimientoSaldoRepository movimientoSaldoRepository;

    public SaldoDiasWriteOperations(
            SaldoDiasRepository saldoDiasRepository,
            MovimientoSaldoRepository movimientoSaldoRepository) {
        this.saldoDiasRepository = saldoDiasRepository;
        this.movimientoSaldoRepository = movimientoSaldoRepository;
    }

    /**
     * @return el saldo actualizado, o null si el eventoId ya fue procesado (no-op idempotente).
     */
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
        if (dias == null) {
            throw new BadRequestException("El evento no incluye una cantidad de dias valida para descontar");
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);
        if (saldoDias == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para el colaborador");
        }

        normalizarCampos(saldoDias);
        saldoDias.setDiasDisponibles(saldoDias.getDiasDisponibles().subtract(dias));
        saldoDias.setDiasPendientes(saldoDias.getDiasPendientes().add(dias));
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

    /**
     * @return el saldo actualizado, o null si el eventoId ya fue procesado (no-op idempotente).
     */
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
        if (dias == null) {
            throw new BadRequestException("El evento no incluye una cantidad de dias valida para devolver");
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);
        if (saldoDias == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para el colaborador");
        }

        normalizarCampos(saldoDias);
        saldoDias.setDiasDisponibles(saldoDias.getDiasDisponibles().add(dias));
        BigDecimal diasPendientesRestantes = saldoDias.getDiasPendientes().subtract(dias);
        saldoDias.setDiasPendientes(diasPendientesRestantes.max(BigDecimal.ZERO.setScale(1)));
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
    public SaldoDiasEntity ejecutarRenovacion(Long saldoId) {
        SaldoDiasEntity saldo = saldoDiasRepository.findById(saldoId);
        if (saldo == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para renovacion");
        }

        return ejecutarProcesoDiario(saldo);
    }

    @Transactional
    public SaldoDiasEntity ejecutarProcesoDiario(Long saldoId) {
        SaldoDiasEntity saldo = saldoDiasRepository.findById(saldoId);
        if (saldo == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para proceso diario");
        }
        return ejecutarProcesoDiario(saldo);
    }

    private SaldoDiasEntity ejecutarProcesoDiario(SaldoDiasEntity saldo) {
        normalizarCampos(saldo);

        int trabajados = saldo.getDiasTrabajados() + 1;
        saldo.setDiasTrabajados(trabajados);

        if (trabajados >= DIAS_PERIODO_ANUAL) {
            BigDecimal nuevoAcumulado = saldo.getDiasAcumulados().add(DIAS_TRUNCOS_POR_PERIODO);
            PoliticaEntity politica = saldo.getPolitica();
            if (politica.getMaxDiasAcumulables() != null) {
                BigDecimal tope = BigDecimal.valueOf(politica.getMaxDiasAcumulables()).setScale(1);
                nuevoAcumulado = nuevoAcumulado.min(tope);
            }
            saldo.setDiasAcumulados(nuevoAcumulado);
            saldo.setDiasTrabajados(0);
        }

        saldoDiasRepository.persist(saldo);
        saldoDiasRepository.getEntityManager().flush();
        return saldo;
    }

    private void normalizarCampos(SaldoDiasEntity saldoDias) {
        if (saldoDias.getDiasDisponibles() == null) {
            saldoDias.setDiasDisponibles(BigDecimal.ZERO.setScale(1));
        }
        if (saldoDias.getDiasUsados() == null) {
            saldoDias.setDiasUsados(BigDecimal.ZERO.setScale(1));
        }
        if (saldoDias.getDiasAcumulados() == null) {
            saldoDias.setDiasAcumulados(BigDecimal.ZERO.setScale(1));
        }
        if (saldoDias.getDiasPendientes() == null) {
            saldoDias.setDiasPendientes(BigDecimal.ZERO.setScale(1));
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
