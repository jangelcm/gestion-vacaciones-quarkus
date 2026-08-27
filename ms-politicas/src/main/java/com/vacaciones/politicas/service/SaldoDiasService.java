package com.vacaciones.politicas.service;

import com.vacaciones.politicas.dto.response.SaldoDiasResponseDto;
import com.vacaciones.politicas.entity.MovimientoSaldoEntity;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.entity.SaldoDiasEntity;
import com.vacaciones.politicas.exception.BadRequestException;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.exception.RuntimeCustomException;
import com.vacaciones.politicas.repository.MovimientoSaldoRepository;
import com.vacaciones.politicas.repository.PoliticaRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class SaldoDiasService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final SaldoDiasRepository saldoDiasRepository;
    private final MovimientoSaldoRepository movimientoSaldoRepository;
    private final PoliticaRepository politicaRepository;

    public SaldoDiasService(
            SaldoDiasRepository saldoDiasRepository,
            MovimientoSaldoRepository movimientoSaldoRepository,
            PoliticaRepository politicaRepository) {
        this.saldoDiasRepository = saldoDiasRepository;
        this.movimientoSaldoRepository = movimientoSaldoRepository;
        this.politicaRepository = politicaRepository;
    }

    @Transactional
    public void asignarPolitica(Long colaboradorId, Long politicaId) {
        SaldoDiasEntity existing = saldoDiasRepository.findByColaboradorId(colaboradorId);
        if (existing != null) {
            throw new RuntimeCustomException(
                    "El colaborador ya tiene una politica asignada",
                    Response.Status.CONFLICT);
        }

        PoliticaEntity politica = politicaRepository.findById(politicaId);
        if (politica == null) {
            throw new ResourceNotFoundException("Politica no encontrada");
        }

        if (!Boolean.TRUE.equals(politica.getActiva())) {
            throw new BadRequestException("La politica no esta activa");
        }

        saldoDiasRepository.persist(SaldoDiasEntity.builder()
                .colaboradorId(colaboradorId)
                .politica(politica)
                .diasDisponibles(BigDecimal.valueOf(politica.getDiasBaseAnio()).setScale(1))
                .diasUsados(BigDecimal.ZERO.setScale(1))
                .diasAcumulados(BigDecimal.ZERO.setScale(1))
                .version(0)
                .build());
    }

    public java.util.List<SaldoDiasResponseDto> getByPoliticaId(Long politicaId) {
        return saldoDiasRepository.findByPoliticaId(politicaId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public SaldoDiasResponseDto getByColaboradorId(Long colaboradorId) {
        SaldoDiasEntity saldoDias = saldoDiasRepository.findByColaboradorId(colaboradorId);

        if (saldoDias == null) {
            throw new ResourceNotFoundException("Saldo no encontrado para el colaborador");
        }

        return toResponseDto(saldoDias);
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

    private SaldoDiasResponseDto toResponseDto(SaldoDiasEntity saldoDias) {
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
}