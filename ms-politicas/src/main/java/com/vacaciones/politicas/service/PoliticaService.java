package com.vacaciones.politicas.service;

import com.vacaciones.politicas.dto.request.PoliticaRequestDto;
import com.vacaciones.politicas.dto.response.PoliticaResponseDto;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.mappers.PoliticaMapper;
import com.vacaciones.politicas.messaging.event.PoliticaActualizadaEvent;
import com.vacaciones.politicas.repository.PoliticaRepository;
import com.vacaciones.politicas.repository.SaldoDiasRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class PoliticaService {

    private static final String POLITICA_NO_ENCONTRADA = "Politica no encontrada";

    private final PoliticaRepository politicaRepository;
    private final SaldoDiasRepository saldoDiasRepository;
    private final Emitter<PoliticaActualizadaEvent> politicaActualizadaEmitter;

    public PoliticaService(
            PoliticaRepository politicaRepository,
            SaldoDiasRepository saldoDiasRepository,
            @Channel("politica-actualizada-out") Emitter<PoliticaActualizadaEvent> politicaActualizadaEmitter) {
        this.politicaRepository = politicaRepository;
        this.saldoDiasRepository = saldoDiasRepository;
        this.politicaActualizadaEmitter = politicaActualizadaEmitter;
    }

    @Transactional
    public PoliticaResponseDto save(PoliticaRequestDto request) {
        PoliticaEntity entity = PoliticaMapper.toEntity(request);
        politicaRepository.persist(entity);
        politicaRepository.getEntityManager().flush();
        publicarPoliticaActualizada(entity);
        return PoliticaMapper.toDto(entity);
    }

    @Transactional
    public PoliticaResponseDto update(Long id, PoliticaRequestDto request) {
        PoliticaEntity existing = findEntityById(id);
        existing.setNombre(request.nombre());
        existing.setTipoVacacion(request.tipoVacacion());
        existing.setDiasBaseAnio(request.diasBaseAnio());
        existing.setAntiguedadMinimaMeses(request.antiguedadMinimaMeses());
        existing.setAcumulable(request.acumulable());
        existing.setMaxDiasAcumulables(request.maxDiasAcumulables());
        existing.setActiva(request.activa());
        politicaRepository.getEntityManager().flush();
        publicarPoliticaActualizada(existing);
        return PoliticaMapper.toDto(existing);
    }

    private void publicarPoliticaActualizada(PoliticaEntity entity) {
        politicaActualizadaEmitter.send(new PoliticaActualizadaEvent(
                entity.getId(),
            null,
            null,
                entity.getNombre(),
                entity.getTipoVacacion(),
                entity.getDiasBaseAnio(),
                entity.getActiva(),
                LocalDateTime.now()));
    }

    public PoliticaResponseDto findById(Long id) {
        return PoliticaMapper.toDto(findEntityById(id));
    }

    @Transactional
    public PoliticaResponseDto delete(Long id) {
        return desactivar(id);
    }

    @Transactional
    public PoliticaResponseDto desactivar(Long id) {
        PoliticaEntity existing = findEntityById(id);

        if (!Boolean.TRUE.equals(existing.getActiva())) {
            return PoliticaMapper.toDto(existing);
        }

        long asignaciones = saldoDiasRepository.countByPoliticaId(id);
        if (asignaciones > 0) {
            existing.setActiva(Boolean.FALSE);
            politicaRepository.getEntityManager().flush();
            publicarPoliticaActualizada(existing);
            return PoliticaMapper.toDto(existing);
        }

        existing.setActiva(Boolean.FALSE);
        politicaRepository.getEntityManager().flush();
        publicarPoliticaActualizada(existing);
        return PoliticaMapper.toDto(existing);
    }

    public List<PoliticaResponseDto> getAll() {
        return politicaRepository.listAll().stream()
                .map(PoliticaMapper::toDto)
                .toList();
    }

    private PoliticaEntity findEntityById(Long id) {
        PoliticaEntity existing = politicaRepository.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException(POLITICA_NO_ENCONTRADA);
        }
        return existing;
    }
}
