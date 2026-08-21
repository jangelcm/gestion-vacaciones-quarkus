package com.vacaciones.politicas.service;

import com.vacaciones.politicas.dto.request.PoliticaRequestDto;
import com.vacaciones.politicas.dto.response.PoliticaResponseDto;
import com.vacaciones.politicas.entity.PoliticaEntity;
import com.vacaciones.politicas.exception.ResourceNotFoundException;
import com.vacaciones.politicas.mappers.PoliticaMapper;
import com.vacaciones.politicas.repository.PoliticaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class PoliticaService {

    private static final String POLITICA_NO_ENCONTRADA = "Politica no encontrada";

    private final PoliticaRepository politicaRepository;

    public PoliticaService(PoliticaRepository politicaRepository) {
        this.politicaRepository = politicaRepository;
    }

    @Transactional
    public PoliticaResponseDto save(PoliticaRequestDto request) {
        PoliticaEntity entity = PoliticaMapper.toEntity(request);
        politicaRepository.persist(entity);
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
        return PoliticaMapper.toDto(existing);
    }

    public PoliticaResponseDto findById(Long id) {
        return PoliticaMapper.toDto(findEntityById(id));
    }

    @Transactional
    public PoliticaResponseDto delete(Long id) {
        PoliticaEntity existing = findEntityById(id);
        PoliticaResponseDto response = PoliticaMapper.toDto(existing);
        politicaRepository.deleteById(id);
        return response;
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
