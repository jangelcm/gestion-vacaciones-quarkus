package com.vacaciones.politicas.mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.vacaciones.politicas.dto.request.PoliticaRequestDto;
import com.vacaciones.politicas.dto.response.PoliticaResponseDto;
import com.vacaciones.politicas.entity.PoliticaEntity;

public final class PoliticaMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private PoliticaMapper() {
    }

    public static PoliticaEntity toEntity(PoliticaRequestDto dto) {
        return PoliticaEntity.builder()
                .nombre(dto.nombre())
                .tipoVacacion(dto.tipoVacacion())
                .diasBaseAnio(dto.diasBaseAnio())
                .antiguedadMinimaMeses(dto.antiguedadMinimaMeses())
                .acumulable(dto.acumulable())
                .maxDiasAcumulables(dto.maxDiasAcumulables())
                .activa(dto.activa())
                .build();
    }

    public static PoliticaResponseDto toDto(PoliticaEntity entity) {
        return new PoliticaResponseDto(
                entity.getId(),
                entity.getNombre(),
                entity.getTipoVacacion(),
                entity.getDiasBaseAnio(),
                entity.getAntiguedadMinimaMeses(),
                entity.getAcumulable(),
                entity.getMaxDiasAcumulables(),
                entity.getActiva(),
                format(entity.getCreatedAt()),
                format(entity.getUpdatedAt()));
    }

    private static String format(LocalDateTime value) {
        return value == null ? null : value.format(FORMATTER);
    }
}