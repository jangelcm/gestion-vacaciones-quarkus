package com.vacaciones.politicas.mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.vacaciones.politicas.dto.request.PoliticaRequestDto;
import com.vacaciones.politicas.dto.response.PoliticaResponseDto;
import com.vacaciones.politicas.entity.PoliticaEntity;

class PoliticaMapperTest {

    @Test
    void shouldConvertRequestDtoToEntity() {
        PoliticaRequestDto dto = new PoliticaRequestDto(
                "Vacaciones anuales",
                "ANUAL",
                15,
                0,
                Boolean.TRUE,
                30,
                Boolean.TRUE);

        PoliticaEntity entity = PoliticaMapper.toEntity(dto);

        assertNull(entity.getId());
        assertEquals("Vacaciones anuales", entity.getNombre());
        assertEquals("ANUAL", entity.getTipoVacacion());
        assertEquals(15, entity.getDiasBaseAnio());
        assertEquals(0, entity.getAntiguedadMinimaMeses());
        assertEquals(Boolean.TRUE, entity.getAcumulable());
        assertEquals(30, entity.getMaxDiasAcumulables());
        assertEquals(Boolean.TRUE, entity.getActiva());
    }

    @Test
    void shouldConvertEntityToResponseDto() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 15, 10, 30, 45);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 15, 11, 15, 30);

        PoliticaEntity entity = PoliticaEntity.builder()
                .id(1L)
                .nombre("Vacaciones premium")
                .tipoVacacion("ANUAL")
                .diasBaseAnio(20)
                .antiguedadMinimaMeses(12)
                .acumulable(Boolean.TRUE)
                .maxDiasAcumulables(40)
                .activa(Boolean.TRUE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        PoliticaResponseDto dto = PoliticaMapper.toDto(entity);

        assertEquals(1L, dto.id());
        assertEquals("Vacaciones premium", dto.nombre());
        assertEquals("ANUAL", dto.tipoVacacion());
        assertEquals(20, dto.diasBaseAnio());
        assertEquals(12, dto.antiguedadMinimaMeses());
        assertEquals(Boolean.TRUE, dto.acumulable());
        assertEquals(40, dto.maxDiasAcumulables());
        assertEquals(Boolean.TRUE, dto.activa());
        assertEquals("15-08-2026 10:30:45", dto.createdAt());
        assertEquals("15-08-2026 11:15:30", dto.updatedAt());
    }

    @Test
    void shouldRejectRequestDtoWhenDiasBaseAnioIsNotPositive() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PoliticaRequestDto(
                        "Vacaciones anuales",
                        "ANUAL",
                        0,
                        0,
                        Boolean.TRUE,
                        30,
                        Boolean.TRUE));

        assertEquals("diasBaseAnio must be greater than 0", exception.getMessage());
    }
}