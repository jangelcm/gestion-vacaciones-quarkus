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

        saldoDias.setDiasDisponibles(saldoDias.getDiasDisponibles().subtract(dias));
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

        saldoDias.setDiasDisponibles(saldoDias.getDiasDisponibles().add(dias));
        // Asuncion: dias_usados nunca queda negativo; si la devolucion excede lo usado, se limita a 0.
        BigDecimal diasUsadosRestantes = saldoDias.getDiasUsados().subtract(dias);
        saldoDias.setDiasUsados(diasUsadosRestantes.max(BigDecimal.ZERO.setScale(diasUsadosRestantes.scale())));
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

        PoliticaEntity politica = saldo.getPolitica();

        BigDecimal nuevoAcumulado = saldo.getDiasAcumulados().add(saldo.getDiasDisponibles());
        if (politica.getMaxDiasAcumulables() != null) {
            BigDecimal tope = BigDecimal.valueOf(politica.getMaxDiasAcumulables()).setScale(nuevoAcumulado.scale());
            nuevoAcumulado = nuevoAcumulado.min(tope);
        }

        saldo.setDiasAcumulados(nuevoAcumulado);
        saldo.setDiasDisponibles(BigDecimal.valueOf(politica.getDiasBaseAnio()).setScale(1));
        saldo.setDiasUsados(BigDecimal.ZERO.setScale(1));

        saldoDiasRepository.persist(saldo);
        saldoDiasRepository.getEntityManager().flush();

        return saldo;
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
