package com.vacaciones.politicas.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import com.vacaciones.politicas.dto.response.SaldoDiasResponseDto;
import com.vacaciones.politicas.entity.MovimientoSaldoEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.repository.MovimientoSaldoRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SaldoDiasService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final SaldoDiasRepository saldoDiasRepository;
    private final MovimientoSaldoRepository movimientoSaldoRepository;

    public SaldoDiasService(
            SaldoDiasRepository saldoDiasRepository,
            MovimientoSaldoRepository movimientoSaldoRepository) {
        this.saldoDiasRepository = saldoDiasRepository;
        this.movimientoSaldoRepository = movimientoSaldoRepository;
    }

    public SaldoDiasResponseDto getByColaboradorId(Long colaboradorId) {
        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);

        if (saldoDias == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para el colaborador");
        }

        return new SaldoDiasResponseDto(
                saldoDias.getId(),
                saldoDias.getColaboradorId(),
                saldoDias.getPolitica() != null ? saldoDias.getPolitica().getId() : null,
                String.valueOf(saldoDias.getDiasDisponibles()),
                String.valueOf(saldoDias.getDiasUsados()),
                String.valueOf(saldoDias.getDiasAcumulados()),
                saldoDias.getCreatedAt() != null ? saldoDias.getCreatedAt().format(FORMATTER) : null,
                saldoDias.getUpdatedAt() != null ? saldoDias.getUpdatedAt().format(FORMATTER) : null);
    }

    @Transactional
    public void descontarDias(
            Long colaboradorId,
            Long solicitudId,
            BigDecimal dias,
            String eventoOrigen,
            String eventoId) {
        if (movimientoSaldoRepository.existsByEventoId(eventoId)) {
            return;
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);
        saldoDias.setDiasDisponibles(saldoDias.getDiasDisponibles().subtract(dias));
        saldoDias.setDiasUsados(saldoDias.getDiasUsados().add(dias));

        persistSaldoOrThrowOnConcurrentUpdate(saldoDias);
        movimientoSaldoRepository.persist(buildMovimiento(
                saldoDias,
                solicitudId,
                "APROBACION",
                dias,
                eventoOrigen,
                eventoId));
    }

    @Transactional
    public void devolverDias(
            Long colaboradorId,
            Long solicitudId,
            BigDecimal dias,
            String eventoOrigen,
            String eventoId) {
        if (movimientoSaldoRepository.existsByEventoId(eventoId)) {
            return;
        }

        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);
        saldoDias.setDiasDisponibles(saldoDias.getDiasDisponibles().add(dias));
        saldoDias.setDiasUsados(saldoDias.getDiasUsados().subtract(dias));

        persistSaldoOrThrowOnConcurrentUpdate(saldoDias);
        movimientoSaldoRepository.persist(buildMovimiento(
                saldoDias,
                solicitudId,
                "CANCELACION",
                dias,
                eventoOrigen,
                eventoId));
    }

    private void persistSaldoOrThrowOnConcurrentUpdate(SaldoDiasEntity saldoDias) {
        Integer currentVersion = saldoDias.getVersion();

        if (currentVersion != null && currentVersion > 0) {
            saldoDiasRepository.persist(saldoDias);
            return;
        }

        saldoDias.setVersion(1);
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